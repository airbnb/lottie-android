package com.airbnb.lottie.compose

import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
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
     * @param ignoreSystemAnimationsDisabled When set to true, the animation will animate even if animations
     *                                       are disabled at the OS level.
     *                                       Defaults to false.
     * @param useCompositionFrameRate Lottie files can specify a target frame rate. By default, Lottie ignores it
     *                                and re-renders on every frame. If that behavior is undesirable, you can set
     *                                this to true to use the composition frame rate instead.
     *                                Note: composition frame rates are usually lower than display frame rates
     *                                so this will likely make your animation feel janky. However, it may be desirable
     *                                for specific situations such as pixel art that are intended to have low frame rates.
     */
    suspend fun animate(
        composition: LottieComposition?,
        iteration: Int = this.iteration,
        iterations: Int = this.iterations,
        reverseOnRepeat: Boolean = this.reverseOnRepeat,
        speed: Float = this.speed,
        clipSpec: LottieClipSpec? = this.clipSpec,
        initialProgress: Float = defaultProgress(composition, clipSpec, speed),
        continueFromPreviousAnimate: Boolean = false,
        cancellationBehavior: LottieCancellationBehavior = LottieCancellationBehavior.Immediately,
        ignoreSystemAnimationsDisabled: Boolean = false,
        useCompositionFrameRate: Boolean = false,
    )
}

@Stable
private class LottieAnimatableImpl : LottieAnimatable {
    override var isPlaying: Boolean by mutableStateOf(false)
        private set

    override val value: Float
        get() = progress

    override var iteration: Int by mutableStateOf(1)
        private set

    override var iterations: Int by mutableStateOf(1)
        private set

    override var reverseOnRepeat: Boolean by mutableStateOf(false)
        private set

    override var clipSpec: LottieClipSpec? by mutableStateOf(null)
        private set

    override var speed: Float by mutableStateOf(1f)
        private set

    override var useCompositionFrameRate: Boolean by mutableStateOf(false)
        private set

    /**
     * Inverse speed value is used to play the animation in reverse when [reverseOnRepeat] is true.
     */
    private val frameSpeed: Float by derivedStateOf {
        if (reverseOnRepeat && iteration % 2 == 0) -speed else speed
    }

    override var composition: LottieComposition? by mutableStateOf(null)
        private set

    private var progressRaw: Float by mutableStateOf(0f)

    override var progress: Float by mutableStateOf(0f)
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
            updateProgress(progress)
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
        reverseOnRepeat: Boolean,
        speed: Float,
        clipSpec: LottieClipSpec?,
        initialProgress: Float,
        continueFromPreviousAnimate: Boolean,
        cancellationBehavior: LottieCancellationBehavior,
        ignoreSystemAnimationsDisabled: Boolean,
        useCompositionFrameRate: Boolean,
    ) {
        mutex.mutate {
            this.iteration = iteration
            this.iterations = iterations
            this.reverseOnRepeat = reverseOnRepeat
            this.speed = speed
            this.clipSpec = clipSpec
            this.composition = composition
            updateProgress(initialProgress)
            this.useCompositionFrameRate = useCompositionFrameRate
            if (!continueFromPreviousAnimate) lastFrameNanos = AnimationConstants.UnspecifiedTime
            if (composition == null) {
                isPlaying = false
                return@mutate
            } else if (speed.isInfinite()) {
                updateProgress(endProgress)
                isPlaying = false
                this.iteration = iterations
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

    private suspend fun doFrame(iterations: Int): Boolean {
        return if (iterations == LottieConstants.IterateForever) {
            // We use withInfiniteAnimationFrameNanos because it allows tests to add a CoroutineContext
            // element that will cancel infinite transitions instead of preventing composition from ever going idle.
            withInfiniteAnimationFrameNanos { frameNanos ->
                onFrame(iterations, frameNanos)
            }
        } else {
            withFrameNanos { frameNanos ->
                onFrame(iterations, frameNanos)
            }
        }
    }

    private fun onFrame(iterations: Int, frameNanos: Long): Boolean {
        val composition = composition ?: return true
        val dNanos = if (lastFrameNanos == AnimationConstants.UnspecifiedTime) 0L else (frameNanos - lastFrameNanos)
        lastFrameNanos = frameNanos

        val minProgress = clipSpec?.getMinProgress(composition) ?: 0f
        val maxProgress = clipSpec?.getMaxProgress(composition) ?: 1f

        val dProgress = dNanos / 1_000_000 / composition.duration * frameSpeed
        val progressPastEndOfIteration = when {
            frameSpeed < 0 -> minProgress - (progressRaw + dProgress)
            else -> progressRaw + dProgress - maxProgress
        }
        if (progressPastEndOfIteration < 0f) {
            updateProgress(progressRaw.coerceIn(minProgress, maxProgress) + dProgress)
        } else {
            val durationProgress = maxProgress - minProgress
            val dIterations = (progressPastEndOfIteration / durationProgress).toInt() + 1

            if (iteration + dIterations > iterations) {
                updateProgress(endProgress)
                iteration = iterations
                return false
            }
            iteration += dIterations
            val progressPastEndRem = progressPastEndOfIteration - (dIterations - 1) * durationProgress
            updateProgress(
                when {
                    frameSpeed < 0 -> maxProgress - progressPastEndRem
                    else -> minProgress + progressPastEndRem
                }
            )
        }

        return true
    }

    private fun Float.roundToCompositionFrameRate(composition: LottieComposition?): Float {
        composition ?: return this
        val frameRate = composition.frameRate
        val interval = 1 / frameRate
        return this - this % interval
    }

    private fun updateProgress(progress: Float) {
        this.progressRaw = progress
        this.progress = if (useCompositionFrameRate) progress.roundToCompositionFrameRate(composition) else progress
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
