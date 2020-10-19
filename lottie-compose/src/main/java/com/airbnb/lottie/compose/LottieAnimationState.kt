package com.airbnb.lottie.compose

import androidx.compose.runtime.*
import kotlinx.coroutines.channels.Channel

@Composable
fun rememberLottieAnimationState(
    autoPlay: Boolean = true,
    repeatCount: Int = 0,
    initialProgress: Float = 0f
): LottieAnimationState {
    return remember(repeatCount, autoPlay) {
        LottieAnimationState(isPlaying = autoPlay, repeatCount, initialProgress)
    }
}

class LottieAnimationState(isPlaying: Boolean, repeatCount: Int = 0, initialProgress: Float = 0f) {
    private var _progress = mutableStateOf(initialProgress)
    val progress: Float by _progress

    private var _frame = mutableStateOf(0)
    val frame: Int by _frame

    var isPlaying by mutableStateOf(isPlaying)
    var repeatCount by mutableStateOf(repeatCount)

    var speed by mutableStateOf(1f)

    internal val updateProgressChannel = Channel<Float>(Channel.CONFLATED)

    fun setProgress(progress: Float) {
        updateProgressChannel.offer(progress)
    }

    internal fun updateProgress(progress: Float, frame: Int) {
        _progress.value = progress
        _frame.value = frame
    }

    fun toggleIsPlaying() {
        isPlaying = !isPlaying
    }
}