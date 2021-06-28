package com.airbnb.lottie.compose

import androidx.compose.animation.core.AnimationConstants
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import com.airbnb.lottie.LottieComposition
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Use this to create a [LottieAnimatable] in a composable.
 *
 * @see LottieAnimatable
 */
@Composable
fun rememberLottieAnimatable(): LottieAnimatable = remember { LottieAnimatable() }

/**
 * Use this to create a [LottieAnimatable] outside of a composable such as a hoisted state class.
 *
 * @see rememberLottieAnimatable
 * @see LottieAnimatable
 */
fun LottieAnimatable(): LottieAnimatable = LottieAnimatableImpl()

/**
 * Reset the animation back to the minimum progress and first iteration.
 */
suspend fun LottieAnimatable.resetToBeginning() {
    snapTo(
        progress = defaultProgress(composition, clipSpec, speed),
        iteration = 1,
    )
}

/**
 * [LottieAnimatable] is an extension of [LottieAnimationState] that contains imperative
 * suspend functions to control animation playback.
 *
 * To create one, call:
 * ```
 * val animatable = rememberLottieAnimatable()
 * ```
 *
 * This is the imperative version of [animateLottieCompositionAsState].
 *
 * [LottieAnimationState] ensures *mutual exclusiveness* on its animations. To
 * achieve this, when a new animation is started via [animate] or [snapTo], any ongoing
 * animation will be canceled via a [CancellationException]. Because of this, it is possible
 * that your animation will not start synchronously. As a result, if you switch from animating
 * one composition to another, it is not safe to render the second composition immediately after
 * calling animate. Instead, you should always rely on [LottieAnimationState.composition] and
 * [LottieAnimationState.progress].
 *
 * This class is comparable to [androidx.compose.animation.core.Animatable]. It is a relatively
 * low-level API that gives maximum control over animations. In most cases, you can use
 * [animateLottieCompositionAsState] which provides declarative APIs to create, update, and animate
 * a [LottieComposition].
 *
 * @see animate
 * @see snapTo
 * @see animateLottieCompositionAsState
 */
@Stable
interface LottieAnimatable : LottieAnimationState {
    /**
     * Snap to a specific point in an animation. This can be used to update the progress
     * or iteration count of an ongoing animation. It will cancel any ongoing animations
     * on this state class. To update and then resume an animation, call [animate] again with
     * continueFromPreviousAnimate set to true after calling [snapTo].
     *
     * @param composition The [LottieComposition] that should be rendered.
     *                    Defaults to [LottieAnimatable.composition].
     * @param progress The progress that should be set.
     *                 Defaults to [LottieAnimatable.progress]
     * @param iteration Updates the current iteration count. This can be used to "rewind" or
     *                  "fast-forward" an ongoing animation to a past/future iteration count.
     *                   Defaults to [LottieAnimatable.iteration]
     * @param resetLastFrameNanos [rememberLottieAnimatable] keeps track of the frame time of the most
     *                            recent animation. When [animate] is called with continueFromPreviousAnimate
     *                            set to true, a delta will be calculated from the most recent [animate] call
     *                            to ensure that the original progress is unaffected by [snapTo] calls in the
     *                            middle.
     *                            Defaults to false if progress is not being snapped to.
     *                            Defaults to true if progress is being snapped to.
     */
    suspend fun snapTo(
        composition: LottieComposition? = this.composition,
        progress: Float = this.progress,
        iteration: Int = this.iteration,
        resetLastFrameNanos: Boolean = progress != this.progress,
    )

    /**
     * Animate a [LottieComposition].
     *
     * @param composition The [LottieComposition] that should be rendered.
     * @param iteration The iteration to start the animation at. Defaults to 1 and carries over from previous animates.
     * @param iterations The number of iterations to continue running for. Set to 1 to play one time
     *                   set to [LottieConstants.IterateForever] to iterate forever. Can be set to arbitrary
     *                   numbers. Defaults to 1 and carries over from previous animates.
     * @param speed The speed at which the composition should be animated. Can be negative. Defaults to 1 and
     *              carries over from previous animates.
     * @param clipSpec An optional [LottieClipSpec] to trim the playback of the composition between two values.
     *                 Defaults to null and carries over from previous animates.
     * @param initialProgress An optional progress value that the animation should start at. Defaults to the
     *                        starting progress as defined by the clipSpec and speed. Because the default value
     *                        isn't the existing progress value, if you are resuming an animation, you
     *                        probably want to set this to [progress].
     * @param continueFromPreviousAnimate When set to true, instead of starting at the minimum progress,
     *                                    the initial progress will be advanced in accordance to the amount
     *                                    of time that has passed since the last frame was rendered.
     * @param cancellationBehavior The behavior that this animation should have when cancelled. In most cases,
     *                             you will want it to cancel immediately. However, if you have a state based
     *                             transition and you want an animation to finish playing before moving on to
     *                             the next one then you may want to set this to [LottieCancellationBehavior.OnIterationFinish].
     */
    suspend fun animate(
        composition: LottieComposition?,
        iteration: Int = this.iteration,
        iterations: Int = this.iterations,
        speed: Float = this.speed,
        clipSpec: LottieClipSpec? = this.clipSpec,
        initialProgress: Float =  defaultProgress(composition, clipSpec, speed),
        continueFromPreviousAnimate: Boolean = false,
        cancellationBehavior: LottieCancellationBehavior = LottieCancellationBehavior.Immediately,
    )
}

@Stable
private class LottieAnimatableImpl : LottieAnimatable {
    override var isPlaying: Boolean by mutableStateOf(false)
        private set

    override var progress: Float by mutableStateOf(0f)
        private set

    override val value: Float
        get() = progress

    override var iteration: Int by mutableStateOf(1)
        private set

    override var iterations: Int by mutableStateOf(1)
        private set

    override var clipSpec: LottieClipSpec? by mutableStateOf(null)
        private set

    override var speed: Float by mutableStateOf(1f)
        private set

    override var composition: LottieComposition? by mutableStateOf(null)
        private set

    override var lastFrameNanos: Long by mutableStateOf(AnimationConstants.UnspecifiedTime)
        private set

    private val endProgress: Float by derivedStateOf {
        val c = composition
        when {
            c == null -> 0f
            speed < 0 -> clipSpec?.getMinProgress(c) ?: 0f
            else -> clipSpec?.getMaxProgress(c) ?: 1f
        }
    }

    override val isAtEnd: Boolean by derivedStateOf { iteration == iterations && progress == endProgress }

    private val mutex = MutatorMutex()

    override suspend fun snapTo(
        composition: LottieComposition?,
        progress: Float,
        iteration: Int,
        resetLastFrameNanos: Boolean,
    ) {
        mutex.mutate {
            this.composition = composition
            this.progress = progress
            this.iteration = iteration
            isPlaying = false
            if (resetLastFrameNanos) {
                lastFrameNanos = AnimationConstants.UnspecifiedTime
            }
        }
    }

    override suspend fun animate(
        composition: LottieComposition?,
        iteration: Int,
        iterations: Int,
        speed: Float,
        clipSpec: LottieClipSpec?,
        initialProgress: Float,
        continueFromPreviousAnimate: Boolean,
        cancellationBehavior: LottieCancellationBehavior,
    ) {
        mutex.mutate {
            require(speed.isFinite()) { "Speed must be a finite number. It is $speed." }
            this.iteration = iteration
            this.iterations = iterations
            this.speed = speed
            this.clipSpec = clipSpec
            this.composition = composition
            this.progress = initialProgress
            if (!continueFromPreviousAnimate) lastFrameNanos = AnimationConstants.UnspecifiedTime
            if (composition == null) {
                isPlaying = false
                return@mutate
            }

            isPlaying = true
            try {
                val context = when (cancellationBehavior) {
                    LottieCancellationBehavior.OnIterationFinish -> NonCancellable
                    LottieCancellationBehavior.Immediately -> EmptyCoroutineContext
                }
                val parentJob = coroutineContext.job
                withContext(context) {
                    while (true) {
                        val actualIterations = when (cancellationBehavior) {
                            LottieCancellationBehavior.OnIterationFinish -> {
                                if (parentJob.isActive) iterations else iteration
                            }
                            else -> iterations
                        }
                        if (!doFrame(actualIterations)) break
                    }
                }
                coroutineContext.ensureActive()
            } finally {
                isPlaying = false
            }
        }
    }

    private suspend fun doFrame(iterations: Int): Boolean = withFrameNanos { frameNanos ->
        val composition = composition ?: return@withFrameNanos true
        val dNanos = if (lastFrameNanos == AnimationConstants.UnspecifiedTime) 0L else (frameNanos - lastFrameNanos)
        lastFrameNanos = frameNanos

        val minProgress = clipSpec?.getMinProgress(composition) ?: 0f
        val maxProgress = clipSpec?.getMaxProgress(composition) ?: 1f

        val dProgress = dNanos / 1_000_000 / composition.duration * speed
        val progressPastEndOfIteration = when {
            speed < 0 -> minProgress - (progress + dProgress)
            else -> progress + dProgress - maxProgress
        }
        if (progressPastEndOfIteration < 0f) {
            progress += dProgress
        } else {
            val durationProgress = maxProgress - minProgress
            val dIterations = (progressPastEndOfIteration / durationProgress).toInt() + 1

            if (iteration + dIterations > iterations) {
                progress = endProgress
                iteration = iterations
                return@withFrameNanos false
            }
            iteration += dIterations
            val progressPastEndRem = progressPastEndOfIteration - (dIterations - 1) * durationProgress
            progress = when {
                speed < 0 -> maxProgress - progressPastEndRem
                else -> minProgress + progressPastEndRem
            }
        }

        true
    }
}

private fun defaultProgress(composition: LottieComposition?, clipSpec: LottieClipSpec?, speed: Float): Float {
    return when {
        speed < 0 && composition == null -> 1f
        composition == null -> 0f
        speed < 0 -> clipSpec?.getMaxProgress(composition) ?: 1f
        else -> clipSpec?.getMinProgress(composition) ?: 0f
    }
}