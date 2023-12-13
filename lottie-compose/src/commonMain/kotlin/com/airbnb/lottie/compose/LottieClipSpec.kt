package com.airbnb.lottie.compose

/**
 * Use subclasses of [LottieClipSpec] to set min/max bounds on the animation playback.
 *
 * @see LottieAnimation
 * @see rememberLottieAnimatable
 * @see animateLottieCompositionAsState
 */
abstract class LottieClipSpec internal constructor() {

    internal abstract fun getMinProgress(composition: LottieComposition): Float

    internal abstract fun getMaxProgress(composition: LottieComposition): Float

    /**
     * Play the animation between these two frames. [maxInclusive] determines whether the animation
     * should play the max frame or stop one frame before it.
     */
    data class Frame(
        val min: Int? = null,
        val max: Int? = null,
        val maxInclusive: Boolean = true,
    ) : LottieClipSpec() {

        private val actualMaxFrame = when {
            max == null -> null
            maxInclusive -> max
            else -> max - 1
        }

        override fun getMinProgress(composition: LottieComposition): Float {
            return when (min) {
                null -> 0f
                else -> (min / composition.lastFrame).coerceIn(0f, 1f)
            }
        }

        override fun getMaxProgress(composition: LottieComposition): Float {
            return when (actualMaxFrame) {
                null -> 1f
                else -> (actualMaxFrame / composition.lastFrame).coerceIn(0f, 1f)
            }
        }
    }

    /**
     * Play the animation between these two progress values.
     */
    data class Progress(
        val min: Float = 0f,
        val max: Float = 1f,
    ) : LottieClipSpec() {
        override fun getMinProgress(composition: LottieComposition): Float {
            return min
        }

        override fun getMaxProgress(composition: LottieComposition): Float {
            return max
        }
    }

    companion object
}
