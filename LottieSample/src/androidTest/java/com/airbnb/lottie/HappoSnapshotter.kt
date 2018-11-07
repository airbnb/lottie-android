package com.airbnb.lottie

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Base64
import android.util.Log
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.*
import com.amazonaws.services.s3.AmazonS3Client
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import io.jsonwebtoken.Jwts.header
import kotlinx.coroutines.*
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.lang.IllegalStateException
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.security.MessageDigest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import com.airbnb.lottie.samples.BuildConfig as BC

private const val TAG = "HappotSnapshotter"

class HappoSnapshotter(
        private val context: Context
) : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val happoApiKey = BC.HappoApiKey
    private val happoSecretKey = BC.HappoSecretKey
    private val gitBranch = if (BC.TRAVIS_GIT_BRANCH.isEmpty()) BC.GIT_BRANCH else BC.TRAVIS_GIT_BRANCH
    private val androidVersion = "android${Build.VERSION.SDK_INT}"
    private val reportNames = listOf(
            "${BC.GIT_SHA}-$androidVersion",
            "$gitBranch-$androidVersion",
            "${BuildConfig.VERSION_NAME}-$androidVersion"
    )

    private val okhttp = OkHttpClient.Builder()
            .addInterceptor { chain ->
                Log.d("Gabe", "INTERCEPTED: ")
                val authenticatedRequest = chain.request().newBuilder()
                        .addHeader("Authentication", Credentials.basic(happoApiKey, happoSecretKey))
                        .build()
                Log.d(TAG, "Added header " + authenticatedRequest.headers())
                chain.proceed(authenticatedRequest)
            }
            .build()

    private val transferUtility = TransferUtility.builder()
            .context(context)
            .s3Client(AmazonS3Client(BasicAWSCredentials(BC.S3AccessKey, BC.S3SecretKey)))
            .defaultBucket("lottie-happo")
            .build()
    private val snapshots = mutableListOf<Snapshot>()

    fun record(animationName: String, bitmap: Bitmap) {
        val md5 = bitmap.md5
        val file = File(context.cacheDir, "$md5.png")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val observer = async { transferUtility.uploadDeferred("snapshots/$md5.png", file) }
        snapshots += Snapshot(observer, bitmap, animationName)
    }

    suspend fun finalizeReportAndUpload() {
        snapshots.forEach { it.await() }
        val json = JsonArray()
        snapshots.forEach {
            json.add(it.toJson())
        }
        reportNames.forEach { upload(it, json) }
    }

    private suspend fun upload(reportName: String, json: JsonElement) {
        val body = RequestBody.create(MediaType.get("application/json"), json.toString())
        val request = Request.Builder()
                .url("https://happo.io/api/reports/$reportName")
//                .header("Authentication", Credentials.basic(happoApiKey, happoSecretKey))
                .post(body)
                .build()

        val response = okhttp.executeDeferred(request)
        if (response.code() == HttpURLConnection.HTTP_OK) {
            Log.d(TAG, "Uploaded $reportName to happo")
        } else {
            throw IllegalStateException("Failed to upload $reportName to Happo. Failed with code ${response.code()}")
        }
    }

    private suspend fun TransferUtility.uploadDeferred(key: String, file: File): TransferObserver = suspendCoroutine { continuation ->
        val observer = transferUtility.upload(key, file)
        val listener = object : TransferListener {
            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {}

            override fun onError(id: Int, ex: Exception) {
                Log.e(TAG, "$id failed uploading!", ex)
                continuation.resumeWithException(ex)
            }

            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    Log.d(TAG, "$id finished uploading.")
                    continuation.resume(observer)
                }
            }
        }
        observer.setTransferListener(listener)
    }

    private val Bitmap.md5
        get() = {
            val outputStream = ByteArrayOutputStream()
            compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val bytes = outputStream.toByteArray()
            val digest = MessageDigest.getInstance("MD5")
            digest.update(bytes, 0, bytes.size)
            BigInteger(1, digest.digest()).toString(16)
        }


    val String.md5: String
        get() {
            val md = MessageDigest.getInstance("MD5")
            return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
        }

    private suspend fun OkHttpClient.executeDeferred(request: Request): Response = suspendCoroutine { continuation ->
        newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }
        })
    }
}