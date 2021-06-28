package com.airbnb.lottie.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.airbnb.lottie.LottieComposition

/**
 * Returns a [LottieAnimationState] representing the progress of an animation.
 *
 * This is the declarative version of [rememberLottieAnimatable] and [LottieAnimation].
 *
 * @param composition The composition to render. This should be retrieved with [rememberLottieComposition].
 * @param isPlaying Whether or not the animation is currently playing. Note that the internal
 *                  animation may end due to reaching the target iterations count. If that happens,
 *                  the animation may stop even if this is still true. You can observe the returned
 *                  [LottieAnimationState.isPlaying] to determine whether the underlying animation
 *                  is still playing.
 * @param restartOnPlay If isPlaying switches from false to true, restartOnPlay determines whether
 *                      the progress and iteration gets reset.
 * @param clipSpec A [LottieClipSpec] that specifies the bound the animation playback
 *                 should be clipped to.
 * @param speed The speed the animation should play at. Numbers larger than one will speed it up.
 *              Numbers between 0 and 1 will slow it down. Numbers less than 0 will play it backwards.
 * @param iterations The number of times the animation should repeat before stopping. It must be
 *                    a positive number. [LottieConstants.IterateForever] can be used to repeat forever.
 */
@Composable
fun animateLottieCompositionAsState(
    composition: LottieComposition?,
    isPlaying: Boolean = true,
    restartOnPlay: Boolean = true,
    clipSpec: LottieClipSpec? = null,
    speed: Float = 1f,
    iterations: Int = 1,
): LottieAnimationState {
    require(iterations > 0) { "Iterations must be a positive number ($iterations)." }
    require(speed.isFinite()) { "Speed must be a finite number. It is $speed." }

    val animatable = rememberLottieAnimatable()
    var wasPlaying by remember { mutableStateOf(isPlaying) }

    LaunchedEffect(
        composition,
        isPlaying,
        clipSpec,
        speed,
        iterations,
    ) {
        if (isPlaying && !wasPlaying && restartOnPlay) {
            animatable.resetToBeginning()
        }
        wasPlaying = isPlaying
        if (!isPlaying) return@LaunchedEffect

        animatable.animate(
            composition,
            iterations = iterations,
            speed = speed,
            clipSpec = clipSpec,
            initialProgress = animatable.progress,
            continueFromPreviousAnimate = false,
        )
    }

    return animatable
}
