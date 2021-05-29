package com.airbnb.lottie.compose

import androidx.compose.runtime.mutableStateOf
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class AnimateLottieCompositionSuspendKtTest {

    private lateinit var composition: LottieComposition

    @Before
    fun setup() {
        composition = LottieCompositionFactory.fromJsonStringSync(Composition.Rect, null).value!!
    }

    @Test
    fun testFrames() = runBlockingTest {
        val progress = mutableStateOf(0f)
        withContext(TestMonotonicFrameClock(this)) {
            val job = launch {
                animateLottieComposition(composition, progress)
            }

            assertEquals(0f, progress.value)
            advanceTimeBy(32)
            assertEquals(0.027f, progress.value, 0.001f)
            advanceTimeBy(64)
            assertEquals(0.134f, progress.value, 0.001f)
            advanceTimeBy(128)
            assertEquals(0.347f, progress.value, 0.001f)
            advanceTimeBy(256)
            assertEquals(0.775f, progress.value, 0.001f)
            advanceTimeBy(127)
            assertEquals(0.962f, progress.value, 0.001f)
            advanceTimeBy(1)
            assertEquals(0.988f, progress.value, 0.001f)
            advanceTimeBy(15)
            assertEquals(0.988f, progress.value, 0.001f)
            advanceTimeBy(1)
            assertEquals(1f, progress.value, 0.001f)
            advanceTimeBy(1000)
            assertEquals(1f, progress.value, 0.001f)

            job.cancel()
        }
    }

    @Test
    fun testFrames2x() = runBlockingTest {
        val progress = mutableStateOf(0f)
        withContext(TestMonotonicFrameClock(this)) {
            val job = launch {
                animateLottieComposition(
                    composition,
                    progress,
                    speed = 2f,
                )
            }

            assertEquals(0f, progress.value)
            advanceTimeBy(32)
            assertEquals(0.053f, progress.value, 0.001f)
            advanceTimeBy(64)
            assertEquals(0.267f, progress.value, 0.001f)
            advanceTimeBy(128)
            assertEquals(0.694f, progress.value, 0.001f)
            advanceTimeBy(95)
            assertEquals(0.961f, progress.value, 0.001f)
            advanceTimeBy(1)
            assertEquals(1f, progress.value, 0.001f)
            advanceTimeBy(1000)
            assertEquals(1f, progress.value, 0.001f)

            job.cancel()
        }
    }

    @Test
    fun testFramesReverse() = runBlockingTest {
        val progress = mutableStateOf(0f)
        withContext(TestMonotonicFrameClock(this)) {
            val job = launch {
                animateLottieComposition(
                    composition,
                    progress,
                    speed = -1f,
                )
            }

            assertEquals(1f - 0f, progress.value)
            advanceTimeBy(32)
            assertEquals(1f - 0.027f, progress.value, 0.001f)
            advanceTimeBy(64)
            assertEquals(1f - 0.134f, progress.value, 0.001f)
            advanceTimeBy(128)
            assertEquals(1f - 0.347f, progress.value, 0.001f)
            advanceTimeBy(256)
            assertEquals(1f - 0.775f, progress.value, 0.001f)
            advanceTimeBy(127)
            assertEquals(1f - 0.962f, progress.value, 0.001f)
            advanceTimeBy(1)
            assertEquals(1f - 0.988f, progress.value, 0.001f)
            advanceTimeBy(15)
            assertEquals(1f - 0.988f, progress.value, 0.001f)
            advanceTimeBy(1)
            assertEquals(1f - 1f, progress.value, 0.001f)
            advanceTimeBy(1000)
            assertEquals(1f - 1f, progress.value, 0.001f)

            job.cancel()
        }
    }

    @Test
    fun testFramesWithClipSpec() = runBlockingTest {
        val progress = mutableStateOf(0f)
        withContext(TestMonotonicFrameClock(this)) {
            val job = launch {
                animateLottieComposition(
                    composition,
                    progress,
                    clipSpec = LottieAnimationClipSpec.MinAndMaxProgress(0.25f, 0.5f)
                )
            }

            assertEquals(0.25f, progress.value)
            advanceTimeBy(32)
            assertEquals(0.277f, progress.value, 0.001f)
            advanceTimeBy(64)
            assertEquals(0.384f, progress.value, 0.001f)
            advanceTimeBy(128)
            assertEquals(0.5f, progress.value, 0.001f)

            job.cancel()
        }
    }
}