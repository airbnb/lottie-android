package com.airbnb.lottie.compose

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest

/**
 * [lottieTransition] allows you to define individual animations mapped to a set of states.
 *
 * Inside of [animate], you will probably want to use the suspending version of [animateLottieComposition].
 *
 * To loop an animation within a transition, just wrap [animateLottieComposition] in a loop.
 *
 * Example usage:
 * ```
 * var state by remember { mutableStateOf(0) }
 * val compositionResult = lottieComposition(LottieCompositionSpec.RawRes(R.raw.your_animation))
 * val progress by lottieTransition(state) { progress ->
 *     val composition = compositionResult.await()
 *     when (state) {
 *         0 -> animateLottieComposition(
 *              composition,
 *              progress,
 *              clipSpec = LottieAnimationClipSpec.MinAndMaxProgress(0f, 0.5f),
 *              cancellationBehavior = LottieCancellationBehavior.AtEnd,
 *          )
 *          1 -> animateLottieComposition(
 *              composition,
 *              progress,
 *              clipSpec = LottieAnimationClipSpec.MinAndMaxProgress(0.5f, 1f),
 *              cancellationBehavior = LottieCancellationBehavior.AtEnd,
 *          )
 *      }
 *  }
 *  LottieAnimation(
 *      compositionResult(),
 *      progress,
 *  )
 * ```
 *
 * @see animateLottieComposition
 */
@Composable
fun <T> lottieTransition(
    state: T,
    animate: suspend CoroutineScope.(progress: MutableState<Float>) -> Unit,
): State<Float> {
    val progress = remember { mutableStateOf(0f) }
    val states = remember { MutableStateFlow(state) }
    states.value  = state

    LaunchedEffect(Unit) {
        // We use collectLatest instead of LaunchedEffect with key = state because
        // we want to allow `animate` to use a NonCancellable CoroutineContext
        // in case it wants to finish its animation before the next state's animation
        // starts.
        // LaunchedEffect won't wait for the NonCancellable job to finish when the
        // new state starts.
        states.collectLatest {
            coroutineScope {
                animate(progress)
            }
        }
    }
    return progress
}