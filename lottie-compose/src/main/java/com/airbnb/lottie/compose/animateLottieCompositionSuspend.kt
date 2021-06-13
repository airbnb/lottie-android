package com.airbnb.lottie.compose

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.withFrameNanos
import com.airbnb.lottie.LottieComposition
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Determines how the animation should behave if the calling CoroutineScope is cancelled.
 */
enum class LottieCancellationBehavior {
    /**
     * Stop animation immediately and return early.
     *
     * @see lottieTransition
     */
    Immediate,

    /**
     * Continue suspending until the animation completes, effectively ignoring the cancellation request.
     * This may be useful if you have a segmented animation with multiple states and you want them to
     * animate smoothly from one to the next when the state changes.
     *
     * @see lottieTransition
     */
    AtEnd,
}

/**
 * This is similar to [animateLottieComposition] which returns progress as a MutableState<Float>.
 * However, instead of returning state, this takes [progress] as a parameter which it updates
 * internally and suspends until the animation completes.
 *
 * To repeat an animation multiple times, just wrap this invocation in a while loop.
 *
 * If the calling CoroutineScope is cancelled, [cancellationBehavior] determines how cancellation
 * should happen. [LottieCancellationBehavior.AtEnd] will ignore the cancellation and continue suspending
 * until the animation completes. [LottieCancellationBehavior.Immediate] will immediately cancel the
 * animation and return early.
 *
 * @return the ending frame time nanos
 *
 * @see lottieTransition
 */
suspend fun animateLottieComposition(
    composition: LottieComposition?,
    progress: MutableState<Float>,
    clipSpec: LottieClipSpec? = null,
    cancellationBehavior: LottieCancellationBehavior = LottieCancellationBehavior.Immediate,
    speed: Float = 1f,
    lastFrameTimeNanos: Long? = null,
    startAtMinProgress: Boolean = true,
): Long {
    require(speed != 0f) { "Speed must not be 0" }
    require(speed.isFinite()) { "Speed must be a finite number. It is $speed." }
    var lastFrameTime = lastFrameTimeNanos ?: withFrameNanos { it }
    composition ?: return lastFrameTime

    val context = when (cancellationBehavior) {
        LottieCancellationBehavior.Immediate -> EmptyCoroutineContext
        LottieCancellationBehavior.AtEnd -> NonCancellable
    }
    withContext(context) {
        val minProgress = clipSpec?.getMinProgress(composition) ?: 0f
        val maxProgress = clipSpec?.getMaxProgress(composition) ?: 1f
        progress.value = when {
            !startAtMinProgress -> progress.value.coerceIn(minProgress, maxProgress)
            speed >= 0 -> minProgress
            else -> maxProgress
        }
        var done = false
        while (!done) {
            withFrameNanos { frameTime ->
                val dTime = (frameTime - lastFrameTime) / TimeUnit.MILLISECONDS.toNanos(1).toFloat()
                lastFrameTime = frameTime
                val dProgress = (dTime * speed) / composition.duration
                val rawProgress = minProgress + ((progress.value - minProgress) + dProgress)
                progress.value = rawProgress.coerceIn(minProgress, maxProgress)
                done = when {
                    speed >= 0 -> rawProgress >= maxProgress
                    else -> rawProgress <= minProgress
                }
            }
        }
    }
    return lastFrameTime
}