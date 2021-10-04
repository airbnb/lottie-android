package com.airbnb.lottie.snapshots.utils

import android.util.Log
import com.airbnb.lottie.L
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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