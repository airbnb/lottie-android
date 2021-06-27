package com.airbnb.lottie.compose

import com.airbnb.lottie.LottieComposition

/**
 * Use subclasses of [LottieClipSpec] to set min/max bounds on the animation playback.
 *
 * @see LottieAnimation
 * @see rememberLottieAnimatable
 * @see animateLottieComposition
 */
sealed class LottieClipSpec {

    internal abstract fun getMinProgress(composition: LottieComposition): Float

    internal abstract fun getMaxProgress(composition: LottieComposition): Float

    /**
     * Play the animation starting from this frame.
     */
    data class MinFrame(val minFrame: Int) : LottieClipSpec() {
        override fun getMinProgress(composition: LottieComposition): Float {
            return (minFrame / composition.endFrame).coerceIn(0f, 1f)
        }

        override fun getMaxProgress(composition: LottieComposition): Float {
            return 1f
        }
    }

    /**
     * Play the animation until this frame.
     */
    data class MaxFrame(val maxFrame: Int, val inclusive: Boolean = true) : LottieClipSpec() {

        private val actualMaxFrame = if (inclusive) maxFrame else maxFrame - 1

        override fun getMinProgress(composition: LottieComposition): Float {
            return 0f
        }

        override fun getMaxProgress(composition: LottieComposition): Float {
            return (actualMaxFrame / composition.endFrame).coerceIn(0f, 1f)
        }
    }

    /**
     * Play the animation between these two frames.
     */
    data class MinAndMaxFrame(val minFrame: Int, val maxFrame: Int, val maxFrameInclusive: Boolean = true) : LottieClipSpec() {

        private val actualMaxFrame = if (maxFrameInclusive) maxFrame else maxFrame - 1

        override fun getMinProgress(composition: LottieComposition): Float {
            return (minFrame / composition.endFrame).coerceIn(0f, 1f)
        }

        override fun getMaxProgress(composition: LottieComposition): Float {
            return (actualMaxFrame / composition.endFrame).coerceIn(0f, 1f)
        }
    }

    /**
     * Play the animation from this progress.
     */
    data class MinProgress(val minProgress: Float) : LottieClipSpec() {
        override fun getMinProgress(composition: LottieComposition): Float {
            return minProgress
        }

        override fun getMaxProgress(composition: LottieComposition): Float {
            return 1f
        }
    }

    /**
     * Play the animation until this progress.
     */
    data class MaxProgress(val maxProgress: Float) : LottieClipSpec() {
        override fun getMinProgress(composition: LottieComposition): Float {
            return 0f
        }

        override fun getMaxProgress(composition: LottieComposition): Float {
            return maxProgress
        }
    }

    /**
     * Play the animation between these two progresses.
     */
    data class MinAndMaxProgress(val minProgress: Float, val maxProgress: Float) : LottieClipSpec() {
        override fun getMinProgress(composition: LottieComposition): Float {
            return minProgress
        }

        override fun getMaxProgress(composition: LottieComposition): Float {
            return maxProgress
        }
    }

    /**
     * Play the animation starting from this marker.
     */
    data class MinMarker(val minMarker: String) : LottieClipSpec() {
        override fun getMinProgress(composition: LottieComposition): Float {
            return ((composition.getMarker(minMarker)?.startFrame ?: 0f) / composition.endFrame).coerceIn(0f, 1f)
        }

        override fun getMaxProgress(composition: LottieComposition): Float {
            return 1f
        }
    }

    /**
     * Play the animation until this marker. If the marker represents the end of your animation, set
     * [playMarkerFrame] to true. If the marker represents the beginning of the next section, set
     * it to false. In that case, the animation will stop at the frame before the marker.
     */
    data class MaxMarker(val maxMarker: String, val playMarkerFrame: Boolean = true) : LottieClipSpec() {
        override fun getMinProgress(composition: LottieComposition): Float {
            return 0f
        }

        override fun getMaxProgress(composition: LottieComposition): Float {
            val offset = if (playMarkerFrame) 0 else -1
            return ((composition.getMarker(maxMarker)?.startFrame?.plus(offset) ?: 0f) / composition.endFrame).coerceIn(0f, 1f)
        }
    }

    /**
     * Play the animation from minMarker until maxMarker. If maxMarker represents the end of your animation,
     * set [playMaxMarkerStartFrame] to true. If the marker represents the beginning of the next section, set
     * it to false. In that case, the animation will stop at the frame before maxMarker.
     */
    data class MinAndMaxMarker(val minMarker: String, val maxMarker: String, val playMaxMarkerStartFrame: Boolean = true) : LottieClipSpec() {
        override fun getMinProgress(composition: LottieComposition): Float {
            return ((composition.getMarker(minMarker)?.startFrame ?: 0f) / composition.endFrame).coerceIn(0f, 1f)
        }

        override fun getMaxProgress(composition: LottieComposition): Float {
            val offset = if (playMaxMarkerStartFrame) 0 else -1
            return ((composition.getMarker(maxMarker)?.startFrame?.plus(offset) ?: 0f) / composition.endFrame).coerceIn(0f, 1f)
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