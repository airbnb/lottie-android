package com.airbnb.lottie.compose

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import com.airbnb.lottie.LottieComposition
import java.util.concurrent.TimeUnit

/**
 * Returns a mutable state representing the progress of an animation.
 *
 * There is also a suspending version of this that takes progress as a MutableState<Float>
 * as a required second parameter.
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
 *                    a positive number. [Integer.MAX_VALUE] can be used to repeat forever.
 * @param onRepeat A callback to be notified every time the animation repeats. Return whether or not the
 *                 animation should continue to repeat.
 * @param onFinished A callback that is invoked when animation completes. Note that the isPlaying parameter you
 *             pass in may still be true. If you want to restart the animation, increase the repeatCount
 *             or change isPlaying to false and then true again.
 */
@Composable
fun animateLottieComposition(
    composition: LottieComposition?,
    isPlaying: Boolean = true,
    restartOnPlay: Boolean = true,
    clipSpec: LottieClipSpec? = null,
    speed: Float = 1f,
    repeatCount: Int = 1,
    onRepeat: ((repeatCount: Int) -> Unit)? = null,
    onFinished: (() -> Unit)? = null,
): MutableState<Float> {
    require(repeatCount > 0) { "Repeat count must be a positive number ($repeatCount)." }
    require(speed != 0f) { "Speed must not be 0" }
    require(speed.isFinite()) { "Speed must be a finite number. It is $speed." }

    val progress = remember { mutableStateOf(0f) }

    var currentRepeatCount by remember { mutableStateOf(0) }
    val currentOnRepeat by rememberUpdatedState(onRepeat)
    val currentOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(composition) {
        progress.value = when (composition) {
            null -> 0f
            else -> if (speed >= 0) clipSpec?.getMinProgress(composition) ?: 0f else clipSpec?.getMaxProgress(composition) ?: 1f
        }
        currentRepeatCount = 0
    }

    LaunchedEffect(composition, isPlaying, repeatCount, clipSpec, speed) {
        if (!isPlaying || composition == null) return@LaunchedEffect
        val minProgress = clipSpec?.getMinProgress(composition) ?: 0f
        val maxProgress = clipSpec?.getMaxProgress(composition) ?: 1f
        if (speed > 0 && (progress.value == 1f || restartOnPlay)) {
            progress.value = minProgress
        } else if (speed < 0 && (progress.value == 0f || restartOnPlay)) {
            progress.value = maxProgress
        }
        if (restartOnPlay || currentRepeatCount >= repeatCount) {
            currentRepeatCount = 0
        }
        var lastFrameTime = withFrameNanos { it }
        var done = false
        while (!done) {
            withFrameNanos { frameTime ->
                val dTime = (frameTime - lastFrameTime) / TimeUnit.MILLISECONDS.toNanos(1).toFloat()
                lastFrameTime = frameTime
                val dProgress = (dTime * speed) / composition.duration
                val rawProgress = minProgress + ((progress.value - minProgress) + dProgress)
                if (speed > 0 && rawProgress > maxProgress) {
                    currentRepeatCount++
                    currentOnRepeat?.invoke(repeatCount)
                } else if (speed < 0 && rawProgress < minProgress) {
                    currentRepeatCount++
                    currentOnRepeat?.invoke(repeatCount)
                }
                done = if (currentRepeatCount < repeatCount && !rawProgress.isInfinite()) {
                    progress.value = minProgress + ((rawProgress - minProgress) fmod (maxProgress - minProgress))
                    false
                } else {
                    progress.value = when {
                        speed >= 0 -> clipSpec?.getMaxProgress(composition) ?: 1f
                        else -> clipSpec?.getMinProgress(composition) ?: 0f
                    }
                    true
                }
            }
        }
        currentOnFinished?.invoke()
    }
    return progress
}

/**
 * Floor mod instead of % which is remainder. This allows negative speeds to properly wrap around to
 * the max progress.
 */
private infix fun Float.fmod(other: Float) = ((this % other) + other) % other