package com.airbnb.lottie.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking

/**
 * @see LottieRetrySignal
 * @see rememberLottieComposition
 */
@Composable
fun rememberLottieRetrySignal(): LottieRetrySignal {
    return remember { LottieRetrySignal() }
}

/**
 * Helper to retry compositions that fail to load. This will mostly happen for animations loaded via url.
 *
 * Call [retry] from an action that should trigger a retry such as a snackbar or retry button.
 *
 * Call [awaitRetry] from the onRetry lambda to [rememberLottieComposition] then return true.
 *
 * @see rememberLottieComposition
 */
@Stable
class LottieRetrySignal internal constructor() {
    private val channel = Channel<Unit>(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    var isAwaitingRetry: Boolean by mutableStateOf(false)
        private set

    fun retry() {
        channel.trySendBlocking(Unit)
    }

    suspend fun awaitRetry() {
        try {
            isAwaitingRetry = true
            channel.receive()
        } finally {
            isAwaitingRetry = false
        }
    }
}