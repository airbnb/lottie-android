package com.airbnb.lottie.compose

import androidx.compose.animation.core.AnimationConstants
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.*
import com.airbnb.lottie.LottieComposition
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * @see LottieAnimatable
 */
fun LottieAnimatable(): LottieAnimatable = LottieAnimatableImpl()

/**
 * [LottieAnimatable] is an extension of [LottieAnimationState] that contains imperative
 * suspend functions to initiate animations.
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
 * [animateLottieComposition] which provides declarative APIs to create, update, and animate
 * a [LottieComposition].
 *
 * @see animate
 * @see snapTo
 */
@Stable
interface LottieAnimatable : LottieAnimationState {
    suspend fun resetToBeginning()

    suspend fun snapTo(
        composition: LottieComposition? = null,
        progress: Float = this.progress,
        iteration: Int = this.iteration,
        resetLastFrameNanos: Boolean = true,
    )

    suspend fun animate(
        composition: LottieComposition?,
        iteration: Int = 1,
        iterations: Int = 1,
        speed: Float = 1f,
        clipSpec: LottieClipSpec? = null,
        initialProgress: Float = defaultProgress(composition, clipSpec, speed),
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

    override suspend fun resetToBeginning() {
        snapTo(
            progress = defaultProgress(composition, clipSpec, speed),
            iteration = 1,
        )
    }

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
                    LottieCancellationBehavior.OnFinish -> NonCancellable
                    LottieCancellationBehavior.Immediately -> EmptyCoroutineContext
                }
                withContext(context) {
                    while (true) {
                        if (!doFrame()) break
                    }
                }
                coroutineContext.ensureActive()
            } finally {
                isPlaying = false
            }
        }
    }

    private suspend fun doFrame(): Boolean = withFrameNanos { frameNanos ->
        val composition = composition ?: return@withFrameNanos true
        val dNanos = if (lastFrameNanos == AnimationConstants.UnspecifiedTime) 0L else (frameNanos - lastFrameNanos)
        lastFrameNanos = frameNanos
        val dProgress = dNanos / 1_000_000 / composition.duration * speed

        val minProgress = clipSpec?.getMinProgress(composition) ?: 0f
        val maxProgress = clipSpec?.getMaxProgress(composition) ?: 1f

        val coercedProgress = progress.coerceIn(minProgress, maxProgress)

        val progressLeft = when {
            speed < 0 -> coercedProgress - minProgress
            else -> maxProgress - coercedProgress
        }

        if (dProgress > progressLeft) {
            val progressPastEnd = dProgress - progressLeft
            val extraIterations = progressPastEnd.toInt()
            val progressPastEndRem = progressPastEnd % 1
            if (iteration + 1 + extraIterations > iterations) {
                // We reached the end.
                progress = when {
                    speed < 0 -> minProgress
                    else -> maxProgress
                }
                iteration = iterations
                return@withFrameNanos false
            }
            iteration += 1 + extraIterations
            progress = when {
                speed < 0 -> maxProgress - progressPastEndRem
                else -> minProgress + progressPastEndRem
            }
        } else {
            progress += dProgress
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