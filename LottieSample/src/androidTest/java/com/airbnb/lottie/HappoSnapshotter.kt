package com.airbnb.lottie

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.*
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.lang.IllegalStateException
import java.math.BigInteger
import java.net.URLEncoder
import java.nio.charset.Charset
import java.security.MessageDigest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import com.airbnb.lottie.samples.BuildConfig as BC

private const val TAG = "HappotSnapshotter"

/**
 * Use this class to record Bitmap snapshots and upload them to happo.
 *
 * To use it:
 *    1) Call record with each bitmap you want to save
 *    2) Call finalizeAndUpload
 */
class HappoSnapshotter(
        private val context: Context
) : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val bucket = "lottie-happo"
    private val happoApiKey = BC.HappoApiKey
    private val happoSecretKey = BC.HappoSecretKey
    private val gitBranch = URLEncoder.encode((if (BC.TRAVIS_GIT_BRANCH == "null") BC.GIT_BRANCH else BC.TRAVIS_GIT_BRANCH).replace("/", "_"), "UTF-8")
    private val androidVersion = "android${Build.VERSION.SDK_INT}"
    private val reportNames = listOfNotNull(
            "${BC.GIT_SHA}-$androidVersion",
            "$gitBranch-$androidVersion",
            "${BuildConfig.VERSION_NAME}-$androidVersion"
    )

    private val okhttp = OkHttpClient()

    private val transferUtility = TransferUtility.builder()
            .context(context)
            .s3Client(AmazonS3Client(BasicAWSCredentials(BC.S3AccessKey, BC.S3SecretKey)))
            .defaultBucket(bucket)
            .build()
    private val snapshots = mutableListOf<Snapshot>()

    fun record(bitmap: Bitmap, animationName: String, variant: String) {
        Log.d(L.TAG, "Recording $animationName $variant")
        val md5 = bitmap.md5
        val key = "snapshots/$md5.png"
        val file = File(context.cacheDir, "$md5.png")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val observer = async { transferUtility.uploadDeferred(key, file) }
        snapshots += Snapshot(observer, bucket, key, bitmap.width, bitmap.height, animationName, variant)
    }

    suspend fun finalizeReportAndUpload() {
        Log.d(L.TAG, "Waiting for snapshots to upload")
        snapshots.forEach { it.await() }
        Log.d(L.TAG, "Finished uploading snapshots")
        val json = JsonObject()
        val snaps = JsonArray()
        json.add("snaps", snaps)
        snapshots.forEach {
            snaps.add(it.toJson())
        }
        reportNames.forEach { upload(it, json) }
    }

    private suspend fun upload(reportName: String, json: JsonElement) {
        val body = RequestBody.create(MediaType.get("application/json"), json.toString())
        val request = Request.Builder()
                .addHeader("Authorization", Credentials.basic(happoApiKey, happoSecretKey, Charset.forName("UTF-8")))
                .url("https://happo.io/api/reports/$reportName")
                .post(body)
                .build()

        val response = okhttp.executeDeferred(request)
        if (response.isSuccessful) {
            Log.d(TAG, "Uploaded $reportName to happo")
        } else {
            throw IllegalStateException("Failed to upload $reportName to Happo. Failed with code ${response.code()}. " + response.body()?.string())
        }
    }

    private suspend fun TransferUtility.uploadDeferred(key: String, file: File): TransferObserver = suspendCoroutine { continuation ->
        val observer = transferUtility.upload(key, file, CannedAccessControlList.PublicRead)
        val listener = object : TransferListener {
            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {}

            override fun onError(id: Int, ex: Exception) {
                Log.e(TAG, "$id failed uploading!", ex)
                continuation.resumeWithException(ex)
            }

            override fun onStateChanged(id: Int, state: TransferState) {
                when (state) {
                    TransferState.COMPLETED -> {
                        Log.d(TAG, "$id finished uploading.")
                        continuation.resume(observer)
                    }
                    TransferState.CANCELED, TransferState.FAILED -> {
                        Log.d(TAG, "$id failed uploading ($state).")
                        continuation.resume(observer)
                    }
                    else -> Unit
                }
            }
        }
        observer.setTransferListener(listener)
    }

    private val Bitmap.md5: String
        get() {
            val outputStream = ByteArrayOutputStream()
            compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val bytes = outputStream.toByteArray()
            val digest = MessageDigest.getInstance("MD5")
            digest.update(bytes, 0, bytes.size)
            return BigInteger(1, digest.digest()).toString(16)
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