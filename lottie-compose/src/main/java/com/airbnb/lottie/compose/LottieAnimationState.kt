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

    /**
     * Enable this to debug slow animations by outlining masks and mattes. The performance overhead of the masks and mattes will
     * be proportional to the surface area of all of the masks/mattes combined.
     * <p>
     * DO NOT leave this enabled in production.
     */
    var outlineMasksAndMattes by mutableStateOf(false)


    /**
     * Sets whether to apply opacity to the each layer instead of shape.
     * <p>
     * Opacity is normally applied directly to a shape. In cases where translucent shapes overlap, applying opacity to a layer will be more accurate
     * at the expense of performance.
     * <p>
     * The default value is false.
     * <p>
     * Note: This process is very expensive and will incur additional performance overhead.
     */
    var applyOpacityToLayers by mutableStateOf(false)

    internal fun updateFrame(frame: Int) {
        _frame.value = frame
    }

    fun toggleIsPlaying() {
        isPlaying = !isPlaying
    }
}