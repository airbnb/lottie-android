package com.airbnb.lottie.snapshots.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import com.airbnb.lottie.L
import com.airbnb.lottie.snapshots.BuildConfig
import com.airbnb.lottie.snapshots.R
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigInteger
import java.net.URLEncoder
import java.nio.charset.Charset
import java.security.KeyStore
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.UUID
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val TAG = "HappoSnapshotter"

/**
 * Use this class to record Bitmap snapshots and upload them to happo.
 *
 * To use it:
 *    1) Call record with each bitmap you want to save
 *    2) Call finalizeAndUpload
 */
class HappoSnapshotter(
    private val context: Context,
    s3AccessKey: String,
    s3SecretKey: String,
    private val happoApiKey: String,
    private val happoSecretKey: String,
    private val onSnapshotRecorded: (snapshotName: String, snapshotVariant: String) -> Unit,
) {
    private val recordJob = Job()
    private val recordScope = CoroutineScope(Dispatchers.IO + recordJob)

    private val bucket = "lottie-happo"
    private val gitBranch = URLEncoder.encode((BuildConfig.GIT_BRANCH).replace("/", "_"), "UTF-8")
    private val androidVersion = "android${Build.VERSION.SDK_INT}"
    private val reportNamePrefixes = listOf(BuildConfig.GIT_SHA, gitBranch, BuildConfig.VERSION_NAME).filter { it.isNotBlank() }

    // Use this when running snapshots locally.
    // private val reportNamePrefixes = listOf(System.currentTimeMillis().toString()).filter { it.isNotBlank() }
    private val reportNames = reportNamePrefixes.map { "$it-$androidVersion" }

    private val okhttp by lazy {
        // https://androiddev.social/@botteaap/112108241212116279
        // https://letsencrypt.org/2023/07/10/cross-sign-expiration.html
        // https://letsencrypt.org/certs/isrgrootx1.der
        val ca: X509Certificate = context.resources.openRawResource(R.raw.isrgrootx1).use {
            CertificateFactory.getInstance("X.509").generateCertificate(it) as X509Certificate
        }

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        keyStore.setCertificateEntry("ca", ca)

        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)

        val sslContext: SSLContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustManagerFactory.trustManagers, null)

        OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManagerFactory.trustManagers[0] as X509TrustManager)
            .build()
    }

    private val transferUtility = TransferUtility.builder()
        .context(context)
        .s3Client(AmazonS3Client(BasicAWSCredentials(s3AccessKey, s3SecretKey)))
        .defaultBucket(bucket)
        .build()
    private val snapshots = mutableListOf<Snapshot>()

    suspend fun record(bitmap: Bitmap, animationName: String, variant: String) = withContext(Dispatchers.IO) {
        val tempUuid = UUID.randomUUID().toString()
        val file = File(context.cacheDir, "$tempUuid.png")

        val fileOutputStream = FileOutputStream(file)
        val byteOutputStream = ByteArrayOutputStream()
        val outputStream = TeeOutputStream(fileOutputStream, byteOutputStream)
        // This is the biggest bottleneck in overall performance. Compress + save can take ~75ms per snapshot.
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val md5 = byteOutputStream.toByteArray().md5
        val key = "snapshots/$md5.png"
        val md5File = File(context.cacheDir, "$md5.png")
        file.renameTo(md5File)

        recordScope.launch { uploadDeferred(key, md5File) }
        Log.d(L.TAG, "Adding snapshot for $animationName-$variant")
        synchronized(snapshots) {
            snapshots += Snapshot(bucket, key, bitmap.width, bitmap.height, animationName, variant)
        }
        onSnapshotRecorded(animationName, variant)
    }

    suspend fun finalizeReportAndUpload() {
        val recordJobStart = System.currentTimeMillis()
        fun Job.activeJobs() = children.filter { it.isActive }.count()
        var activeJobs = recordJob.activeJobs()
        while (activeJobs > 0) {
            activeJobs = recordJob.activeJobs()
            Log.d(L.TAG, "Waiting for record $activeJobs jobs to finish.")
            delay(1000)
        }
        recordJob.children.forEach { it.join() }
        Log.d(L.TAG, "Waited ${System.currentTimeMillis() - recordJobStart}ms for recordings to finish saving.")
        val json = JsonObject()
        val snaps = JsonArray()
        json.add("snaps", snaps)
        snapshots.forEach { s ->
            snaps.add(s.toJson())
        }
        Log.d(L.TAG, "Finished creating snapshot report")
        reportNames.forEach { reportName ->
            Log.d(L.TAG, "Uploading $reportName")
            upload(reportName, json)
        }
    }

    private suspend fun upload(reportName: String, json: JsonElement) {
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .addHeader("Authorization", Credentials.basic(happoApiKey, happoSecretKey, Charset.forName("UTF-8")))
            .url("https://happo.io/api/reports/$reportName")
            .post(body)
            .build()

        val response = okhttp.executeDeferred(request)
        if (response.isSuccessful) {
            Log.d(TAG, "Uploaded $reportName to happo")
        } else {
            throw IllegalStateException("Failed to upload $reportName to Happo. Failed with code ${response.code}. " + response.body?.string())
        }
    }

    private suspend fun uploadDeferred(key: String, file: File): TransferObserver {
        return retry { _, _ ->
            transferUtility.upload(key, file, CannedAccessControlList.PublicRead).await()
        }
    }

    private suspend fun OkHttpClient.executeDeferred(request: Request): Response = suspendCoroutine { continuation ->
        newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }
        })
    }

    private val ByteArray.md5: String
        get() {
            val digest = MessageDigest.getInstance("MD5")
            digest.update(this, 0, this.size)
            return BigInteger(1, digest.digest()).toString(16)
        }
}
