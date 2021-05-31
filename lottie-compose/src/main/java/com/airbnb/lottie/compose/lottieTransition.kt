package com.airbnb.lottie.compose

import androidx.compose.runtime.*
import com.airbnb.lottie.LottieComposition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest

/**
 * [lottieTransition] allows you to define individual animations mapped to a set of states.
 *
 * Use this version if your transition is defined by separate segments from within a single composition
 *
 * Inside of [animate], you will probably want to use the suspending version of [animateLottieComposition].
 *
 * To loop an animation within a transition, just wrap [animateLottieComposition] in a loop.
 *
 * Example usage:
 * ```
 * var state by remember { mutableStateOf(0) }
 * val compositionResult = lottieComposition(LottieCompositionSpec.RawRes(R.raw.your_animation))
 * val progress = lottieTransition(state) { progress ->
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
): Float {
    val progress = remember { mutableStateOf(0f) }
    val states = remember { MutableStateFlow(state) }
    states.value  = state
    val currentAnimate by rememberUpdatedState(animate)

    LaunchedEffect(Unit) {
        // We use collectLatest instead of LaunchedEffect with key = state because
        // we want to allow `animate` to use a NonCancellable CoroutineContext
        // in case it wants to finish its animation before the next state's animation
        states.collectLatest {
            coroutineScope {
                currentAnimate(progress)
            }
        }
    }
    return progress.value
}

/**
 * State returned from [lottieTransition] when your transition consists of multiple compositions.
 */
class LottieTransitionState {
    @Suppress("PropertyName")
    internal val _progress = mutableStateOf(0f)

    @Suppress("PropertyName")
    internal val _composition = mutableStateOf(LottieCompositionResult())

    val composition: LottieCompositionResult get() = _composition.value

    val progress: Float get() = _progress.value

    operator fun component1() = composition

    operator fun component2() = progress
}

/**
 * [lottieTransition] allows you to define individual animations mapped to a set of states.
 *
 * Use this version if your transition is split up into several compositions.
 *
 * Inside of [animate], you will probably want to use the suspending version of [animateLottieComposition].
 *
 * To loop an animation within a transition, just wrap [animateLottieComposition] in a loop.
 *
 * Example usage:
 * ```
 * var state by remember { mutableStateOf(0) }
 * val compositionResult1 = lottieComposition(LottieCompositionSpec.RawRes(R.raw.your_animation_1))
 * val compositionResult2 = lottieComposition(LottieCompositionSpec.RawRes(R.raw.your_animation_2))
 *
 * val (compositionResult, progress) = lottieTransition(
 *     state,
 *     compositionForState = {
 *         when (state) {
 *             0 -> compositionResult1
 *             else -> compositionResult2
 *         }
 *     },
 * ) { compositionResult, progress ->
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
    compositionForState: suspend () -> LottieCompositionResult,
    animate: suspend CoroutineScope.(composition: LottieCompositionResult, progress: MutableState<Float>) -> Unit,
): LottieTransitionState {
    val transitionState = remember { LottieTransitionState() }
    val states = remember { MutableStateFlow(state) }
    states.value  = state
    val currentCompositionForState by rememberUpdatedState(compositionForState)
    val currentAnimate by rememberUpdatedState(animate)

    LaunchedEffect(Unit) {
        // We use collectLatest instead of LaunchedEffect with key = state because
        // we want to allow `animate` to use a NonCancellable CoroutineContext
        // in case it wants to finish its animation before the next state's animation
        // starts.
        // LaunchedEffect won't wait for the NonCancellable job to finish when the
        // new state starts.
        states.collectLatest {
            val compositionResult = currentCompositionForState()
            transitionState._composition.value = compositionResult
            coroutineScope {
                currentAnimate(compositionResult, transitionState._progress)
            }
        }
    }
    return transitionState
}
