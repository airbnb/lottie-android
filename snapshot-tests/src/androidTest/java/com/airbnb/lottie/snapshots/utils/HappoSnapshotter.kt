package com.airbnb.lottie.snapshots.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import com.airbnb.lottie.L
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Use this class to record Bitmap snapshots and upload them to happo.
 *
 * To use it:
 *    1) Call record with each bitmap you want to save
 *    2) Call finalizeAndUpload
 */
class HappoSnapshotter(
    private val context: Context,
    private val onSnapshotRecorded: (snapshotName: String, snapshotVariant: String) -> Unit,
) {
    private val bucket = "lottie-happo"
    private val cacheDir by lazy {
        val file = File("/sdcard/Download", "lottie")
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw IllegalStateException("Unable to make cache dir.")
            }
        }
        file
    }
    private val snapshotTempDir by lazy {
        val file = File(cacheDir, "snapshots-temp")
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw IllegalStateException("Unable to make cache dir.")
            }
        }
        file
    }
    private val snapshotDir by lazy {
        val file = File(cacheDir, "snapshots")
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw IllegalStateException("Unable to make cache dir.")
            }
        }
        file
    }

    private val snapshots = mutableListOf<Snapshot>()

    suspend fun record(bitmap: Bitmap, animationName: String, variant: String) = withContext(Dispatchers.IO) {
        val tempUuid = UUID.randomUUID().toString()
        val file = File(snapshotTempDir, "$tempUuid.png")

        val fileOutputStream = FileOutputStream(file)
        val byteOutputStream = ByteArrayOutputStream()
        val outputStream = TeeOutputStream(fileOutputStream, byteOutputStream)
        // This is the biggest bottleneck in overall performance. Compress + save can take ~75ms per snapshot.
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val md5 = byteOutputStream.toByteArray().md5
        val key = "snapshots/$md5.png"
        val md5File = File(snapshotDir, "$md5.png")
        if (!file.renameTo(md5File)) {
            throw IllegalStateException("Unable to rename ${file.absolutePath} to ${md5File.absolutePath}")
        }
        Log.d("Gabe", "Renamed file to ${md5File.absolutePath}")

        Log.d(L.TAG, "Adding snapshot for $animationName-$variant")
        synchronized(snapshots) {
            snapshots += Snapshot(bucket, key, bitmap.width, bitmap.height, animationName, variant)
        }
        onSnapshotRecorded(animationName, variant)
    }

    fun setupCacheDir() {
        val files = cacheDir.listFiles() ?: return
        for (file in files) {
            file.deleteRecursively()
        }
    }

    fun finalizeReportAndUpload() {
        val recordJobStart = System.currentTimeMillis()
        Log.d(L.TAG, "Waited ${System.currentTimeMillis() - recordJobStart}ms for recordings to finish saving.")
        val json = JsonObject()
        val snaps = JsonArray()
        json.add("snaps", snaps)
        snapshots.forEach { s ->
            snaps.add(s.toJson())
        }
        Log.d(L.TAG, "Finished creating snapshot report")
        val reportFile = File(cacheDir, "report.json")
        FileOutputStream(reportFile).use { fos ->
            fos.write(json.toString().toByteArray())
        }
        createZip()
    }

    private val ByteArray.md5: String
        get() {
            val digest = MessageDigest.getInstance("MD5")
            digest.update(this, 0, this.size)
            return BigInteger(1, digest.digest()).toString(16)
        }

    private fun createZip() {
        val files = (snapshotDir.listFiles() ?: emptyArray()) + File(cacheDir, "report.json")
        ZipOutputStream(BufferedOutputStream(FileOutputStream("/sdcard/Download/snapshots.zip"))).use { out ->
            for (file in files) {
                FileInputStream(file).use { fi ->
                    BufferedInputStream(fi).use { origin ->
                        val entryName = file.absolutePath.substring(file.absolutePath.lastIndexOf("/"))
                        val entry = ZipEntry(entryName)
                        out.putNextEntry(entry)
                        origin.copyTo(out, 1024)
                    }
                }
            }
        }
    }
}