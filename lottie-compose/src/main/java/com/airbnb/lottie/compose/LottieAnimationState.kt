package com.airbnb.lottie.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberLottieAnimationState(
    autoPlay: Boolean = true,
    repeatCount: Int = 0,
    initialProgress: Float = 0f,
): LottieAnimationState {
    return remember(repeatCount, autoPlay) {
        LottieAnimationState(isPlaying = autoPlay, repeatCount, initialProgress)
    }
}

/**
 * @see rememberLottieAnimationState()
 */
class LottieAnimationState(
    isPlaying: Boolean,
    repeatCount: Int = 0,
    initialProgress: Float = 0f,
) {
    var progress by mutableStateOf(initialProgress)

    private var _frame = mutableStateOf(0)
    val frame: Int by _frame

    var isPlaying by mutableStateOf(isPlaying)
    var repeatCount by mutableStateOf(repeatCount)

    var speed by mutableStateOf(1f)

    internal fun updateFrame(frame: Int) {
        _frame.value = frame
    }

    fun toggleIsPlaying() {
        isPlaying = !isPlaying
    }
}