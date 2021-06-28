package com.airbnb.lottie.compose

import com.airbnb.lottie.LottieComposition

/**
 * Use subclasses of [LottieClipSpec] to set min/max bounds on the animation playback.
 *
 * @see LottieAnimation
 * @see rememberLottieAnimatable
 * @see animateLottieCompositionAsState
 */
sealed class LottieClipSpec {

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
                else -> (min / composition.endFrame).coerceIn(0f, 1f)
            }
        }

        override fun getMaxProgress(composition: LottieComposition): Float {
            return when (actualMaxFrame) {
                null -> 1f
                else -> (actualMaxFrame / composition.endFrame).coerceIn(0f, 1f)
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

    /**
     * Play the animation from minMarker until maxMarker. If maxMarker represents the end of your animation,
     * set [maxInclusive] to true. If the marker represents the beginning of the next section, set
     * it to false to stop the animation at the frame before maxMarker.
     */
    data class Markers(
        val min: String? = null,
        val max: String? = null,
        val maxInclusive: Boolean = true
    ) : LottieClipSpec() {
        override fun getMinProgress(composition: LottieComposition): Float {
            return when (min) {
                null -> 0f
                else -> ((composition.getMarker(min)?.startFrame ?: 0f) / composition.endFrame).coerceIn(0f, 1f)
            }
        }

        override fun getMaxProgress(composition: LottieComposition): Float {
            return when (max) {
                null -> 1f
                else -> {
                    val offset = if (maxInclusive) 0 else -1
                    return ((composition.getMarker(max)?.startFrame?.plus(offset) ?: 0f) / composition.endFrame).coerceIn(0f, 1f)
                }
            }
        }
    }

    /**
     * Play the animation from the beginning of the marker for the duration of the marker itself.
     * The duration can be set in After Effects.
     */
    data class Marker(val marker: String) : LottieClipSpec() {
        override fun getMinProgress(composition: LottieComposition): Float {
            return ((composition.getMarker(marker)?.startFrame ?: 0f) / composition.endFrame).coerceIn(0f, 1f)
        }

        override fun getMaxProgress(composition: LottieComposition): Float {
            val marker = composition.getMarker(marker) ?: return 1f
            return ((marker.startFrame + marker.durationFrames) / composition.endFrame).coerceIn(0f, 1f)
        }
    }
}