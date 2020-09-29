package com.airbnb.lottie.samples

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import com.airbnb.lottie.L
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.Charset
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
) {
    private val recordJob = Job()
    val recordContext: CoroutineContext
        get() = Dispatchers.IO + recordJob
    val recordScope = CoroutineScope(recordContext)

    private val bucket = "lottie-happo"
    private val happoApiKey = BC.HappoApiKey
    private val happoSecretKey = BC.HappoSecretKey
    private val gitBranch = URLEncoder.encode((if (BC.BITRISE_GIT_BRANCH == "null") BC.GIT_BRANCH else BC.BITRISE_GIT_BRANCH).replace("/", "_"), "UTF-8")
    private val androidVersion = "android${Build.VERSION.SDK_INT}"
    private val reportNamePrefixes = listOf(BC.GIT_SHA, gitBranch, BC.VERSION_NAME).filter { it.isNotBlank() }
    private val reportNames = reportNamePrefixes.map { "$it-$androidVersion" }

    private val okhttp = OkHttpClient()

    private val transferUtility = TransferUtility.builder()
            .context(context)
            .s3Client(AmazonS3Client(BasicAWSCredentials(BC.S3AccessKey, BC.S3SecretKey)))
            .defaultBucket(bucket)
            .build()
    private val snapshots = mutableListOf<Snapshot>()

    suspend fun record(bitmap: Bitmap, animationName: String, variant: String) = withContext(Dispatchers.IO) {
        val md5 = bitmap.md5
        val key = "snapshots/$md5.png"
        val file = File(context.cacheDir, "$md5.png")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        recordScope.async { transferUtility.uploadDeferred(key, file) }
        snapshots += Snapshot(bucket, key, bitmap.width, bitmap.height, animationName, variant)
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
        snapshots.forEach {
            snaps.add(it.toJson())
        }
        reportNames.forEach { upload(it, json) }
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

    private suspend fun TransferUtility.uploadDeferred(key: String, file: File): TransferObserver {
        return transferUtility.upload(key, file, CannedAccessControlList.PublicRead).await()
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
}