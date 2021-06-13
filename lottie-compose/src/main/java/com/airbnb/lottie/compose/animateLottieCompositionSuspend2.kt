package com.airbnb.lottie.compose

import androidx.compose.runtime.*
import com.airbnb.lottie.LottieComposition
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.job
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@Stable
class LottieAnimationState : State<Float> {
    private val currentJob = AtomicReference<Job?>(null)

    override var value: Float by mutableStateOf(0f)
        private set

    var composition: LottieComposition? by mutableStateOf(null)
        private set

    var minProgress: Float by mutableStateOf(0f)
        private set

    var maxProgress: Float by mutableStateOf(0f)
        private set

    var isPlaying: Boolean by mutableStateOf(false)
        private set

    var repeatCount: Int by mutableStateOf(1)
        private set

    var targetRepeatCount: Int by mutableStateOf(1)

    var speed: Float by mutableStateOf(1f)

    suspend fun toggleIsPlaying() {
        runJob {
            if (isPlaying) {
                pause()
            } else {
                resume()
            }
        }
    }

    suspend fun pause() {
        runJob {
            // Cancel any ongoing animation
        }
    }

    suspend fun resume() {
        animateImpl(composition, minProgress, maxProgress, repeatCount, speed, resetRepeatCount = true)
    }

    suspend fun snapTo(composition: LottieComposition?, progress: Float) {
        runJob {
            updateProperties(
                composition = composition,
                progress = progress,
                isPlaying = false,
                repeatCount = 1,
                targetRepeatCount = 1,
                speed = 1f,
                minProgress = 0f,
                maxProgress = 1f,
            )
        }
    }

    suspend fun animate(
        composition: LottieComposition?,
        minProgress: Float = value,
        maxProgress: Float = 1f,
        repeatCount: Int = 1,
        speed: Float = 1f,
    ) {
        animateImpl(composition, minProgress, maxProgress, repeatCount, speed, resetRepeatCount = true)
    }

    private suspend fun animateImpl(
        composition: LottieComposition?,
        minProgress: Float = value,
        maxProgress: Float = 1f,
        repeatCount: Int = 1,
        speed: Float = 1f,
        resetRepeatCount: Boolean = true,
    ) {
        runJob {
            require(speed != 0f) { "Speed must not be 0" }
            require(speed.isFinite()) { "Speed must be a finite number. It is $speed." }
            composition ?: return@runJob
            try {
                updateProperties(
                    composition = composition,
                    progress = if (speed >= 0) minProgress else maxProgress,
                    isPlaying = true,
                    repeatCount = if (resetRepeatCount) 1 else this.repeatCount,
                    targetRepeatCount = repeatCount,
                    speed = speed,
                    minProgress = minProgress,
                    maxProgress = maxProgress,
                )
                var lastFrameTime = withFrameNanos { it }
                var done = false
                while (!done) {
                    withFrameNanos { frameTime ->
                        val dTime = (frameTime - lastFrameTime) / TimeUnit.MILLISECONDS.toNanos(1).toFloat()
                        lastFrameTime = frameTime
                        val dProgress = (dTime * speed) / composition.duration
                        val rawProgress = minProgress + ((value - minProgress) + dProgress)
                        if (speed >= 0 && rawProgress > maxProgress) {
                            this.repeatCount++
                        } else if (speed < 0 && rawProgress < minProgress) {
                            this.repeatCount++
                        }
                        done = if (this.repeatCount < this.targetRepeatCount && !rawProgress.isInfinite()) {
                            value = minProgress + ((rawProgress - minProgress) fmod (maxProgress - minProgress))
                            false
                        } else {
                            value = when {
                                speed >= 0 -> maxProgress
                                else -> minProgress
                            }
                            true
                        }
                    }
                }
            } finally {
                isPlaying = false
            }
        }
    }

    @Synchronized
    private fun updateProperties(
        composition: LottieComposition?,
        progress: Float,
        isPlaying: Boolean,
        repeatCount: Int,
        targetRepeatCount: Int,
        speed: Float,
        minProgress: Float,
        maxProgress: Float,
    ) {
        this.composition = composition
        this.value = progress
        this.isPlaying = isPlaying
        this.repeatCount = repeatCount
        this.targetRepeatCount = targetRepeatCount
        this.speed = speed
        this.minProgress = minProgress
        this.maxProgress = maxProgress
    }

    private suspend fun runJob(block: suspend () -> Unit) {
        val oldJob = currentJob.get()
        oldJob?.cancelAndJoin()
        coroutineScope {
            val newJob = coroutineContext.job
            // This should be set to null in the finally block below while this joined the cancellation.
            currentJob.compareAndSet(null, newJob)
            try {
                block()
            } finally {
                currentJob.compareAndSet(newJob, null)
            }
        }
    }
}

/**
 * Floor mod instead of % which is remainder. This allows negative speeds to properly wrap around to
 * the max progress.
 */
private infix fun Float.fmod(other: Float) = ((this % other) + other) % other