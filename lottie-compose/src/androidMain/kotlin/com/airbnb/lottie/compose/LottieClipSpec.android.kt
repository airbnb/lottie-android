package com.airbnb.lottie.compose


/**
 * Play the animation from minMarker until maxMarker. If maxMarker represents the end of your animation,
 * set [maxInclusive] to true. If the marker represents the beginning of the next section, set
 * it to false to stop the animation at the frame before maxMarker.
 */
fun LottieClipSpec.Companion.Markers(
    min: String? = null,
    max: String? = null,
    maxInclusive: Boolean = true
) : LottieClipSpec = MarkersImpl(
    min = min,
    max = max,
    maxInclusive = maxInclusive
)

fun LottieClipSpec.Companion.Marker(marker: String) : LottieClipSpec =
    MarkerImpl(marker)

private data class MarkersImpl(
    val min: String? = null,
    val max: String? = null,
    val maxInclusive: Boolean = true
) : LottieClipSpec() {
    override fun getMinProgress(composition: LottieComposition): Float {
        return when (min) {
            null -> 0f
            else -> ((composition.getMarker(min)?.startFrame ?: 0f) / composition.lastFrame).coerceIn(0f, 1f)
        }
    }

    override fun getMaxProgress(composition: LottieComposition): Float {
        return when (max) {
            null -> 1f
            else -> {
                val offset = if (maxInclusive) 0 else -1
                return ((composition.getMarker(max)?.startFrame?.plus(offset) ?: 0f) / composition.lastFrame).coerceIn(0f, 1f)
            }
        }
    }
}

/**
 * Play the animation from the beginning of the marker for the duration of the marker itself.
 * The duration can be set in After Effects.
 */
private data class MarkerImpl(val marker: String) : LottieClipSpec() {
    override fun getMinProgress(composition: LottieComposition): Float {
        return ((composition.getMarker(marker)?.startFrame ?: 0f) / composition.lastFrame).coerceIn(0f, 1f)
    }

    override fun getMaxProgress(composition: LottieComposition): Float {
        val marker = composition.getMarker(marker) ?: return 1f
        return ((marker.startFrame + marker.durationFrames) / composition.lastFrame).coerceIn(0f, 1f)
    }
}
