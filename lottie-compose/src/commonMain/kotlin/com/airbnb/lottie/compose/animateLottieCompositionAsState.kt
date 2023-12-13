package com.airbnb.lottie.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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
 * @param reverseOnRepeat Defines what this animation should do when it reaches the end. This setting
 *                        is applied only when [iterations] is either greater than 0 or [LottieConstants.IterateForever].
 *                        Defaults to `false`.
 * @param clipSpec A [LottieClipSpec] that specifies the bound the animation playback
 *                 should be clipped to.
 * @param speed The speed the animation should play at. Numbers larger than one will speed it up.
 *              Numbers between 0 and 1 will slow it down. Numbers less than 0 will play it backwards.
 * @param iterations The number of times the animation should repeat before stopping. It must be
 *                    a positive number. [LottieConstants.IterateForever] can be used to repeat forever.
 * @param cancellationBehavior The behavior that this animation should have when cancelled. In most cases,
 *                             you will want it to cancel immediately. However, if you have a state based
 *                             transition and you want an animation to finish playing before moving on to
 *                             the next one then you may want to set this to [LottieCancellationBehavior.OnIterationFinish].
 * @param ignoreSystemAnimatorScale By default, Lottie will respect the system animator scale set in developer options or set to 0
 *                                  by things like battery saver mode. When set to 0, the speed will effectively become [Integer.MAX_VALUE].
 *                                  Set this to false if you want to ignore the system animator scale and always default to normal speed.
 */
@Composable
fun animateLottieCompositionAsState(
    composition: LottieComposition?,
    isPlaying: Boolean = true,
    restartOnPlay: Boolean = true,
    reverseOnRepeat: Boolean = false,
    clipSpec: LottieClipSpec? = null,
    speed: Float = 1f,
    iterations: Int = 1,
    cancellationBehavior: LottieCancellationBehavior = LottieCancellationBehavior.Immediately,
    ignoreSystemAnimatorScale: Boolean = false,
    useCompositionFrameRate: Boolean = false,
): LottieAnimationState {
    require(iterations > 0) { "Iterations must be a positive number ($iterations)." }
    require(speed.isFinite()) { "Speed must be a finite number. It is $speed." }

    val animatable = rememberLottieAnimatable()
    var wasPlaying by remember { mutableStateOf(isPlaying) }

    // Dividing by 0 correctly yields Float.POSITIVE_INFINITY here.
    val actualSpeed = if (ignoreSystemAnimatorScale) speed else (speed / systemAnimationScale())

    LaunchedEffect(
        composition,
        isPlaying,
        clipSpec,
        actualSpeed,
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
            reverseOnRepeat = reverseOnRepeat,
            speed = actualSpeed,
            clipSpec = clipSpec,
            initialProgress = animatable.progress,
            continueFromPreviousAnimate = false,
            cancellationBehavior = cancellationBehavior,
            useCompositionFrameRate = useCompositionFrameRate,
        )
    }

    return animatable
}

@Composable
internal expect fun systemAnimationScale() : Float
