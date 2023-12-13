package com.airbnb.lottie.compose

/**
 * Use subclasses of [LottieClipSpec] to set min/max bounds on the animation playback.
 *
 * @see LottieAnimation
 * @see rememberLottieAnimatable
 * @see animateLottieCompositionAsState
 */
expect sealed class LottieClipSpec() {

    internal abstract fun getMinProgress(composition: LottieComposition): Float

    internal abstract fun getMaxProgress(composition: LottieComposition): Float

    companion object
}

/**
 * Play the animation between these two frames. [maxInclusive] determines whether the animation
 * should play the max frame or stop one frame before it.
 */
expect fun LottieClipSpec.Companion.Frame(
    min: Int? = null,
    max: Int? = null,
    maxInclusive: Boolean = true
) : LottieClipSpec

/**
 * Play the animation between these two progress values.
 */
expect fun LottieClipSpec.Companion.Progress(
    min: Float = 0f,
    max: Float = 1f,
) : LottieClipSpec
