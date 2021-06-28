package com.airbnb.lottie.compose

import androidx.compose.animation.core.AnimationConstants
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import com.airbnb.lottie.LottieComposition

/**
 * [LottieAnimationState] is a value holder that contains information about the current Lottie animation.
 *
 * The primary values are [LottieAnimationState.progress] and [LottieAnimationState.composition]. These
 * value should be passed into the main [LottieAnimation] composable.
 *
 * @see progress
 * @see composition
 * @see animateLottieCompositionAsState
 */
@Stable
interface LottieAnimationState : State<Float> {
    val isPlaying: Boolean

    val progress: Float

    val iteration: Int

    val iterations: Int

    val clipSpec: LottieClipSpec?

    val speed: Float

    val composition: LottieComposition?

    val lastFrameNanos: Long get() = AnimationConstants.UnspecifiedTime

    val isAtEnd: Boolean
}