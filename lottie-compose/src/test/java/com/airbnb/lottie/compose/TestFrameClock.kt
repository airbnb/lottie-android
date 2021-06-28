package com.airbnb.lottie.compose

import androidx.compose.runtime.MonotonicFrameClock
import kotlinx.coroutines.channels.Channel

/**
 * This class is original from:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/animation/animation-core/src/test/java/androidx/compose/animation/core/SuspendAnimationTest.kt;l=211
 */
class TestFrameClock : MonotonicFrameClock {
    // Make the send non-blocking
    private val frameChannel = Channel<Long>(Channel.UNLIMITED)

    private suspend fun frame(frameTimeNanos: Long) {
        frameChannel.send(frameTimeNanos)
    }

    suspend fun frameMs(frameTimeMs: Long) {
        frame(frameTimeMs * 1_000_000L)
    }

    override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
        return onFrame(frameChannel.receive())
    }
}