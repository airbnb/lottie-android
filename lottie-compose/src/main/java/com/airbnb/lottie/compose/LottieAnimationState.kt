package com.airbnb.lottie.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Create a [LottieAnimationState] and remember it
 *
 * @param autoPlay Initial value for [LottieAnimationState.isPlaying]
 * @param repeatCount Initial value for [LottieAnimationState.repeatCount]
 * @param initialProgress Initial value for [LottieAnimationState.progress]
 * @param enableMergePaths Initial value for [LottieAnimationState.enableMergePaths]
 */
@Composable
fun rememberLottieAnimationState(
    autoPlay: Boolean = true,
    repeatCount: Int = 0,
    initialProgress: Float = 0f,
    enableMergePaths: Boolean = true
): LottieAnimationState {
    // Use rememberSavedInstanceState so you can pause/resume animations
    return remember(repeatCount, autoPlay) {
        LottieAnimationState(
            isPlaying = autoPlay,
            repeatCount = repeatCount,
            initialProgress = initialProgress,
            enableMergePaths = enableMergePaths
        )
    }
}

/**
 * State of the [LottieAnimation] composable
 *
 * @param isPlaying Initial value for [isPlaying]
 * @param repeatCount Initial value for [repeatCount]
 * @param initialProgress Initial value for [progress]
 * @param enableMergePaths Initial value for [enableMergePaths]
 *
 * @see rememberLottieAnimationState
 */
class LottieAnimationState(
    isPlaying: Boolean,
    repeatCount: Int = 0,
    initialProgress: Float = 0f,
    enableMergePaths: Boolean = true
) {
    var progress by mutableStateOf(initialProgress)

    // TODO: make this public
    private var _frame = mutableStateOf(0)
    val frame: Int by _frame

    /**
     * Whether the animation is currently playing.
     */
    var isPlaying by mutableStateOf(isPlaying)

    /**
     * How many times the animation will be played. Use [Int.MAX_VALUE] for
     * infinite repetitions.
     */
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

    /**
     * Enable this to get merge path support.
     * <p>
     * Merge paths currently don't work if the the operand shape is entirely contained within the
     * first shape. If you need to cut out one shape from another shape, use an even-odd fill type
     * instead of using merge paths.
     * <p>
     * If your animation contains merge paths and you are encountering rendering issues, disabling
     * merge paths might help.
     */
    var enableMergePaths by mutableStateOf(enableMergePaths)

    internal fun updateFrame(frame: Int) {
        _frame.value = frame
    }

    fun toggleIsPlaying() {
        isPlaying = !isPlaying
    }
}