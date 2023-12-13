package com.airbnb.lottie.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier


/**
 * This is the base LottieAnimation composable. It takes a composition and renders it at a specific progress.
 *
 * The overloaded version of [LottieAnimation] that handles playback and is sufficient for most use cases.
 *
 * @param composition The composition that will be rendered. To generate a [LottieComposition], you can use
 *                    [rememberLottieComposition].
 * @param progress A provider for the progress (between 0 and 1) that should be rendered. If you want to render a
 *                         specific frame, you can use [LottieComposition.getFrameForProgress]. In most cases, you will want
 *                         to use one of the overloaded LottieAnimation composables that drives the animation for you.
 *                         The overloads that have isPlaying as a parameter instead of progress will drive the
 *                         animation automatically. You may want to use this version if you want to drive the animation
 *                         from your own Animatable or via events such as download progress or a gesture.
 *                     For more details, refer to the docs of [AsyncUpdates].
 */
@Composable
expect fun LottieAnimation(
    composition : LottieComposition?,
    progress : () -> Float,
    modifier: Modifier = Modifier
)

/**
 * This is like [LottieAnimation] except that it handles driving the animation via [animateLottieCompositionAsState]
 * instead of taking a progress provider.
 *
 * @see LottieAnimation
 * @see animateLottieCompositionAsState
 */
@Composable
fun LottieAnimation(
    composition: LottieComposition?,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    restartOnPlay: Boolean = true,
    clipSpec: LottieClipSpec? = null,
    speed: Float = 1f,
    iterations: Int = 1,
    reverseOnRepeat: Boolean = false,
) {
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        restartOnPlay = restartOnPlay,
        clipSpec = clipSpec,
        reverseOnRepeat = reverseOnRepeat,
        speed = speed,
        iterations = iterations,
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}


