package com.airbnb.lottie.compose

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import com.airbnb.lottie.LottieComposition
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class LottieAnimationState internal constructor(initialIsPlaying: Boolean) : State<Float> {
    var isPlaying: Boolean by mutableStateOf(initialIsPlaying)
        internal set

    override var value: Float by mutableStateOf(0f)
        internal set

    var currentRepeatCount: Int by mutableStateOf(1)
        internal set

    internal val actionChannel = Channel<LottieAnimationAction>()

    internal val onFinished = MutableSharedFlow<Long>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    suspend fun restart() {
        actionChannel.send(LottieAnimationAction.Reset)
        actionChannel.send(LottieAnimationAction.Resume)
    }

    suspend fun toggleIsPlaying() {
        actionChannel.send(if (isPlaying) LottieAnimationAction.Pause else LottieAnimationAction.Resume)
    }

    suspend fun pause() {
        actionChannel.send(LottieAnimationAction.Pause)
    }

    suspend fun resume() {
        actionChannel.send(LottieAnimationAction.Resume)
    }

    suspend fun snapTo(progress: Float) {
        actionChannel.send(LottieAnimationAction.SnapTo(progress))
    }

    /**
     * Suspends until the animation finishes and then returns the last frame time nanos.
     */
    suspend fun awaitFinished(): Long {
        return onFinished.first()
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
 * @param isPlaying Whether or not the animation is currently playing. Note that the internal
 *                  animation may end due to reaching the target repeatCount. If that happens,
 *                  the animation may stop even if this is still true. You may want to use
 *                  onFinished to set isPlaying to false but in many cases, it won't matter.
 * @param restartOnPlay If isPlaying switches from false to true, restartOnPlay determines whether
 *                      the progress and repeatCount get reset.
 * @param clipSpec A [LottieClipSpec] that specifies the bound the animation playback
 *                 should be clipped to.
 * @param speed The speed the animation should play at. Numbers larger than one will speed it up.
 *              Numbers between 0 and 1 will slow it down. Numbers less than 0 will play it backwards.
 * @param repeatCount The number of times the animation should repeat before stopping. It must be
 *                    a positive number. [LottieConstants.repeatForever] can be used to repeat forever.
 * @param onRepeat An optional callback to be notified every time the animation repeats.
 * @param onFinished An optional callback that is invoked when animation completes. Note that the isPlaying
 *                   parameter you pass in may still be true. If you want to restart the animation, increase the
 *                   repeatCount or change isPlaying to false and then true again.
 */
@Composable
fun animateLottieComposition(
    composition: LottieComposition?,
    initialIsPlaying: Boolean = true,
    clipSpec: LottieClipSpec? = null,
    speed: Float = 1f,
    repeatCount: Int = 1,
): LottieAnimationState {
    require(repeatCount > 0) { "Repeat count must be a positive number ($repeatCount)." }
    require(speed != 0f) { "Speed must not be 0" }
    require(speed.isFinite()) { "Speed must be a finite number. It is $speed." }
    val state = remember { LottieAnimationState(initialIsPlaying) }

    LaunchedEffect(composition) {
        state.value = when (composition) {
            null -> 0f
            else -> when {
                speed >= 0 -> clipSpec?.getMinProgress(composition) ?: 0f
                else -> clipSpec?.getMaxProgress(composition) ?: 1f
            }
        }
        state.currentRepeatCount = 0
    }

    LaunchedEffect(state) {
        for (action in state.actionChannel) {
            when (action) {
                LottieAnimationAction.Reset -> {
                    state.value = when {
                        composition == null -> 0f
                        speed > 0 -> clipSpec?.getMinProgress(composition) ?: 0f
                        else -> clipSpec?.getMaxProgress(composition) ?: 1f
                    }
                }
                LottieAnimationAction.Pause -> {
                    state.isPlaying = false
                }
                LottieAnimationAction.Resume -> {
                    state.isPlaying = true
                }
                is LottieAnimationAction.SnapTo -> {
                    state.value = when {
                        composition == null -> 0f
                        clipSpec == null -> action.progress
                        else -> action.progress.coerceIn(
                            clipSpec.getMinProgress(composition),
                            clipSpec.getMaxProgress(composition),
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(composition, state.isPlaying, repeatCount, clipSpec, speed) {
        if (!state.isPlaying || composition == null) return@LaunchedEffect
        val minProgress = clipSpec?.getMinProgress(composition) ?: 0f
        val maxProgress = clipSpec?.getMaxProgress(composition) ?: 1f
        if (speed >= 0 && (state.value == 1f)) {
            state.value = minProgress
        } else if (speed < 0 && (state.value == 0f)) {
            state.value = maxProgress
        }
        var lastFrameTime = withFrameNanos { it }
        var done = false
        while (!done) {
            withFrameNanos { frameTime ->
                val dTime = (frameTime - lastFrameTime) / TimeUnit.MILLISECONDS.toNanos(1).toFloat()
                lastFrameTime = frameTime
                val dProgress = (dTime * speed) / composition.duration
                val rawProgress = minProgress + ((state.value - minProgress) + dProgress)
                if (speed > 0 && rawProgress > maxProgress) {
                    state.currentRepeatCount++
                } else if (speed < 0 && rawProgress < minProgress) {
                    state.currentRepeatCount++
                }
                done = if (state.currentRepeatCount < repeatCount && !rawProgress.isInfinite()) {
                    state.value = minProgress + ((rawProgress - minProgress) fmod (maxProgress - minProgress))
                    false
                } else {
                    state.value = when {
                        speed >= 0 -> clipSpec?.getMaxProgress(composition) ?: 1f
                        else -> clipSpec?.getMinProgress(composition) ?: 0f
                    }
                    true
                }
            }
        }
        state.onFinished.emit(lastFrameTime)
    }
    return state
}

/**
 * Floor mod instead of % which is remainder. This allows negative speeds to properly wrap around to
 * the max progress.
 */
private infix fun Float.fmod(other: Float) = ((this % other) + other) % other

internal sealed class LottieAnimationAction {
    object Reset : LottieAnimationAction()
    object Pause : LottieAnimationAction()
    object Resume : LottieAnimationAction()
    class SnapTo(val progress: Float) : LottieAnimationAction()
}