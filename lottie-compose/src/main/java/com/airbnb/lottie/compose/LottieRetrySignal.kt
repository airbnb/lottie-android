package com.airbnb.lottie.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking

/**
 * @see LottieRetrySignal
 * @see lottieComposition
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
 * Call [awaitRetry] from the onRetry lambda to [lottieComposition] then return true.
 *
 * @see lottieComposition
 */
class LottieRetrySignal internal constructor() {
    private val channel = Channel<Unit>(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun retry() {
        channel.sendBlocking(Unit)
    }

    suspend fun awaitRetry() {
        channel.receive()
    }
}