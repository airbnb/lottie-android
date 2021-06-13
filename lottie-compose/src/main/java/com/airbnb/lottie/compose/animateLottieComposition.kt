package com.airbnb.lottie.compose

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import com.airbnb.lottie.LottieComposition
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.atomic.AtomicReference

enum class LottiePlayMode {
    Play,
    Pause;

    operator fun not(): LottiePlayMode = if (this == Play) Pause else Play
}

@Composable
fun rememberLottieAnimationState(
    iterations: Int = 1,
    clipSpec: LottieClipSpec? = null,
    speed: Float = 1f,
): LottieAnimationState {
    val state = remember { LottieAnimationState() }
    LaunchedEffect(iterations) {
        state.iterations = iterations
    }
    LaunchedEffect(clipSpec) {
        state.clipSpec = clipSpec
    }
    LaunchedEffect(speed) {
        state.speed = speed
    }
    return state
}

@Stable
class LottieAnimationState internal constructor() : MutableState<Float> {
    private var job = AtomicReference<Job?>()

    private val updatesChannel = Channel<Unit>()

    private var _value by mutableStateOf(0f)
    override var value: Float
        get() = _value
        set(value) {
            _value = value
            updatesChannel.offer(Unit)
        }

    var isPlaying: Boolean by mutableStateOf(false)
        private set

    var iteration: Int by mutableStateOf(1)

    private var _iterations: Int by mutableStateOf(1)
    var iterations: Int
        get() = _iterations
        set(value) {
            _iterations = value
            iteration = minOf(iteration, value)
            updatesChannel.offer(Unit)
        }

    var _clipSpec: LottieClipSpec? by mutableStateOf(null)
    var clipSpec: LottieClipSpec?
        get() = _clipSpec
        set(value) {
            _clipSpec = value
            updatesChannel.offer(Unit)
        }

    var speed: Float by mutableStateOf(1f)

    var isAtEnd by mutableStateOf(true)
        private set

    /**
     * Animate the Lottie composition given the state properties above.
     * If the animation reaches the end, instead of finishing,
     */
    suspend fun animate(
        composition: LottieComposition?,
        cancellationBehavior: LottieCancellationBehavior = LottieCancellationBehavior.Immediate,
    ): Unit = coroutineScope {
        val oldJob = job.get()
        oldJob?.cancelAndJoin()
        if (composition == null) {
            isPlaying = false
            return@coroutineScope
        }
        val newJob = coroutineContext.job
        job.compareAndSet(oldJob, newJob)
        isPlaying = true
        try {
            animateImpl(composition, cancellationBehavior)
        } finally {
            isPlaying = false
        }
    }

    private suspend fun animateImpl(
        composition: LottieComposition,
        cancellationBehavior: LottieCancellationBehavior,
    ) {
        val minProgress = clipSpec?.getMinProgress(composition) ?: 0f
        val maxProgress = clipSpec?.getMaxProgress(composition) ?: 1f
        _value = value.coerceIn(minProgress, maxProgress)
        var lastFrameTimeNanos = withFrameNanos { it }
        while (true) {
            awaitReadyToAnimate(maxProgress)
            lastFrameTimeNanos = animateLottieComposition(
                composition,
                progress = this@LottieAnimationState,
                clipSpec = clipSpec,
                speed = speed,
                lastFrameTimeNanos = lastFrameTimeNanos,
                startAtMinProgress = false,
                cancellationBehavior = cancellationBehavior,
            )
            if (iteration < iterations) {
                iteration++
                _value = when {
                    speed >= 0 -> minProgress
                    else -> maxProgress
                }
            }
        }
    }

    private suspend fun awaitReadyToAnimate(maxProgress: Float) {
        if (iteration < iterations || value < maxProgress) {
            isAtEnd = false
            return
        }
        isPlaying = false
        isAtEnd = true
        for (u in updatesChannel) {
            if (iteration < iterations || value < maxProgress) {
                isAtEnd = false
                isPlaying = true
                return
            }
        }
    }

    override fun component1(): Float {
        return value
    }

    override fun component2(): (Float) -> Unit {
        return { value = it }
    }
}

/**
 * Returns a mutable state representing the progress of an animation.
 *
 * Because the state is mutable, you can modify its value and the internal animation
 * will continue animating from the value you set. The progress will snap to the value you
 * set without changing the repeat count.
 *
 * There is also a suspending version of this that takes progress as a MutableState<Float>
 * as a required second parameter.
 *
 * You do not have to use this to animate a Lottie composition. You may create your own animation
 * and pass its progress to [LottieComposition].
 *
 * @param composition The composition to render. This should be retrieved with [lottieComposition].
 * @param playMode Whether or not the Lottie animation should be playing if it is not at the end of
 *                 the animation.
 * @param clipSpec A [LottieClipSpec] that specifies the bound the animation playback
 *                 should be clipped to.
 * @param speed The speed the animation should play at. Numbers larger than one will speed it up.
 *              Numbers between 0 and 1 will slow it down. Numbers less than 0 will play it backwards.
 * @param iterations The number of times the animation should repeat before stopping. It must be
 *                    a positive number. [Integer.MAX_VALUE] can be used to repeat forever.
 */
@Composable
fun animateLottieComposition(
    composition: LottieComposition?,
    playMode: LottiePlayMode = LottiePlayMode.Play,
    iterations: Int = 1,
    clipSpec: LottieClipSpec? = null,
    speed: Float = 1f,
): LottieAnimationState {
    val state = rememberLottieAnimationState(
        iterations = iterations,
        clipSpec = clipSpec,
        speed = speed,
    )
    LaunchedEffect(composition, playMode) {
        if (playMode == LottiePlayMode.Play) state.animate(composition)
    }
    return state
}
