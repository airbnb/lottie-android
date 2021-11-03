package com.airbnb.lottie.snapshots.utils

import android.util.Log
import com.airbnb.lottie.L
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
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

suspend fun <T> retry(
    delayMs: Long = 2_000,
    maxRetryCount: Int = 10,
    canRetry: (Throwable) -> Boolean = { true },
    block: suspend (tryCount: Int, previousException: Throwable?) -> T,
): T {
    var previousException: Throwable? = null
    for (i in 1..maxRetryCount) {
        val result = runCatching { block(i, previousException) }
        result.onSuccess { value ->
            return value
        }
        result.onFailure { e ->
            if (e is CancellationException || i == maxRetryCount || !canRetry(e)) throw e
            previousException = e
        }
        delay(delayMs)
    }
    error("Max retries exceeded but the last invocation did not fail.")
}