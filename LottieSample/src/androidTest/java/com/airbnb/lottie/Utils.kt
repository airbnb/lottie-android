package com.airbnb.lottie

import android.graphics.Bitmap
import android.util.Log
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

val Bitmap.md5: String
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

suspend fun TransferObserver.await() = suspendCoroutine<TransferObserver> { continuation ->
    val listener = object : TransferListener {
        override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {}

        override fun onError(id: Int, ex: Exception) {
            Log.e(L.TAG, "$id failed uploading!", ex)
            continuation.resumeWithException(ex)
        }

        override fun onStateChanged(id: Int, state: TransferState) {
            when (state) {
                TransferState.COMPLETED -> {
                    Log.d(L.TAG, "$id finished uploading.")
                    continuation.resume(this@await)
                }
                TransferState.CANCELED, TransferState.FAILED -> {
                    Log.d(L.TAG, "$id failed uploading ($state).")
                    continuation.resume(this@await)
                }
                else -> Unit
            }
        }
    }
    setTransferListener(listener)
}