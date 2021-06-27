package com.airbnb.lottie.compose

import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LottieAnimatableImplTest {

    private lateinit var clock: TestFrameClock
    private lateinit var anim: LottieAnimatable
    private lateinit var composition: LottieComposition
    private val compositionDuration get() = composition.duration.toLong()

    @Before
    fun setup() {
        clock = TestFrameClock()
        anim = LottieAnimatable()
        composition = LottieCompositionFactory.fromJsonStringSync(CompositionFixtures.Rect, null).value!!
    }

    @Test
    fun testSingleIterationProgress() = runTest {
        launch {
            anim.animate(composition)
        }
        assertEquals(0f, anim.progress)
        clock.frameMs(0)
        clock.frameMs(300)
        assertEquals(0.5f, anim.progress, 0.01f)
        clock.frameMs(composition.duration.toLong() - 1)
        assertFalse(anim.isAtEnd)
        assertTrue(anim.isPlaying)
        clock.frameMs(composition.duration.toLong())
        assertTrue(anim.isAtEnd)
        assertFalse(anim.isPlaying)
    }

    @Test
    fun testJumpFromOneIterationToEndOfNext() = runTest {
        launch {
            anim.animate(composition, iterations = 2)
        }
        assertEquals(0f, anim.progress)
        clock.frameMs(0)

        assertFrame(300, progress = 0.5f, iterations = 2)
        assertFrame(compositionDuration - 1, progress = 0.998f, iteration = 1, iterations = 2)
        assertFrame(2 * compositionDuration, progress = 1f, iteration = 2, iterations = 2, isPlaying = false, isAtEnd = true)
    }

    @Test
    fun testTwoIterations() = runTest {
        launch {
            anim.animate(composition, iterations = 2)
        }
        assertEquals(0f, anim.progress)
        clock.frameMs(0)

        assertFrame(300, progress = 0.5f, iterations = 2)
        assertFrame(compositionDuration - 1, progress = 0.998f, iteration = 1, iterations = 2)
        assertFrame(compositionDuration, progress = 0f, iteration = 2, iterations = 2)
        assertFrame((2 * compositionDuration) - 1, progress = 0.998f, iteration = 2, iterations = 2)
        assertFrame(2 * compositionDuration, progress = 1f, iteration = 2, iterations = 2, isPlaying = false, isAtEnd = true)
    }

    @Test
    fun testJumpsFromOneIterationToThree() = runTest {
        val job = launch {
            anim.animate(composition, iterations = 3)
        }
        assertEquals(0f, anim.progress)
        clock.frameMs(0)

        assertFrame(2 * compositionDuration + 300, progress = 0.5f, iteration = 3, iterations = 3)
        job.cancel()
    }

    @Test
    fun testCancels() = runTest {
        val job = launch {
            anim.animate(composition)
        }
        assertEquals(0f, anim.progress)
        clock.frameMs(0)

        assertFrame(300, progress = 0.5f)
        job.cancel()
        assertFrame(compositionDuration - 1, progress = 0.5f, isPlaying = false, lastFrameNanos = 300000000L)
    }

    @Test
    fun testReverse() = runTest {
        launch {
            anim.animate(composition, speed = -1f)
        }
        assertFrame(0, progress = 1f, speed = -1f)
        assertFrame(200, progress = 2 / 3f, speed = -1f)
        assertFrame(compositionDuration - 1, progress = 0.0016f, speed = -1f)
        assertFrame(compositionDuration, progress = 0f, isAtEnd = true, isPlaying = false, speed = -1f)
    }

    @Test
    fun testClipSpec() = runTest {
        val clipSpec = LottieClipSpec.Progress(0.25f, 0.75f)
        launch {
            anim.animate(composition, clipSpec = clipSpec)
        }
        assertFrame(0, progress = 0.25f, clipSpec = clipSpec)
        assertFrame(299, progress = 0.749f, clipSpec = clipSpec)
        assertFrame(300, progress = 0.75f, isPlaying = false, isAtEnd = true, clipSpec = clipSpec)
    }

    @Test
    fun testClipSpecWithTwoIterations() = runTest {
        val clipSpec = LottieClipSpec.Progress(0.25f, 0.75f)
        launch {
            anim.animate(composition, clipSpec = clipSpec, iterations = 2)
        }
        assertFrame(0, progress = 0.25f, clipSpec = clipSpec, iterations = 2)
        assertFrame(299, progress = 0.749f, clipSpec = clipSpec, iterations = 2)
        assertFrame(598, progress = 0.748f, iteration = 2, clipSpec = clipSpec, iterations = 2)
        assertFrame(599, progress = 0.75f, iteration = 2, isPlaying = false, isAtEnd = true, clipSpec = clipSpec, iterations = 2)
    }

    @Test
    fun testNegativeSpeedWithClipSpec() = runTest {
        val clipSpec = LottieClipSpec.Progress(0.25f, 0.75f)
        launch {
            anim.animate(composition, clipSpec = clipSpec, speed = -1f)
        }
        assertFrame(0, progress = 0.75f, clipSpec = clipSpec, speed = -1f)
        assertFrame(299, progress = 0.2508f, clipSpec = clipSpec, speed = -1f)
        assertFrame(300, progress = 0.25f, isPlaying = false, isAtEnd = true, clipSpec = clipSpec, speed = -1f)
    }

    @Test
    fun testChangingEndClipSpec() = runTest {
        launch {
            anim.animate(composition)
        }
        assertFrame(0, progress = 0f)
        assertFrame(300, progress = 0.5f)
        val clipSpec = LottieClipSpec.Progress(max = 0.75f)
        launch {
            anim.animate(composition, clipSpec = clipSpec, continueFromPreviousAnimate = false)
        }
        assertFrame(316, progress = 0f, clipSpec = clipSpec)
        assertFrame(616, progress = 0.5f, clipSpec = clipSpec)
        assertFrame(800, progress = 0.75f, clipSpec = clipSpec, isPlaying = false, isAtEnd = true)
    }

    @Test
    fun testChangingBeginningClipSpec() = runTest {
        launch {
            anim.animate(composition, iterations = 2)
        }
        assertFrame(0, progress = 0f, iterations = 2)
        assertFrame(300, progress = 0.5f, iterations = 2)
        val clipSpec = LottieClipSpec.Progress(min = 0.25f)
        launch {
            anim.animate(composition, clipSpec = clipSpec, initialProgress = anim.progress, continueFromPreviousAnimate = true)
        }
        assertFrame(598, progress = 0.998f, clipSpec = clipSpec, iterations = 2)
        assertFrame(599, progress = 0.25f, clipSpec = clipSpec, iteration = 2, iterations = 2)
        assertFrame(1048, progress = 0.999f, clipSpec = clipSpec, iteration = 2, iterations = 2)
        assertFrame(1049, progress = 1f, clipSpec = clipSpec, iteration = 2, iterations = 2, isPlaying = false, isAtEnd = true)
    }

    @Test
    fun testResumingAnimation() = runTest {
        launch {
            anim.animate(composition)
        }
        assertFrame(0, progress = 0f)
        assertFrame(300, progress = 0.5f)
        val clipSpec = LottieClipSpec.Progress(max = 0.75f)
        launch {
            anim.animate(
                composition,
                clipSpec = clipSpec,
                initialProgress = anim.progress,
                continueFromPreviousAnimate = true,
            )
        }
        assertFrame(316, progress = 0.528f, clipSpec = clipSpec)
        assertFrame(449, progress = 0.749f, clipSpec = clipSpec)
        assertFrame(450, progress = 0.75f, clipSpec = clipSpec, isPlaying = false, isAtEnd = true)
    }

    @Test
    fun testReRunAnimation() = runTest {
        launch {
            anim.animate(composition)
        }
        assertFrame(0, progress = 0f)
        assertFrame(300, progress = 0.5f)
        launch {
            anim.animate(composition, initialProgress = anim.progress, continueFromPreviousAnimate = true)
        }
        assertFrame(300, progress = 0.5f)
        assertFrame(598, progress = 0.998f)
        assertFrame(599, progress = 1f, isPlaying = false, isAtEnd = true)
    }

    @Test
    fun testSnapNoopToThenResume() = runTest {
        launch {
            anim.animate(composition)
        }
        assertFrame(0, progress = 0f)
        assertFrame(300, progress = 0.5f)
        launch {
            anim.snapTo(composition)
            anim.animate(composition, initialProgress = anim.progress, continueFromPreviousAnimate = true)
        }
        assertFrame(300, progress = 0.5f)
        assertFrame(598, progress = 0.998f)
        assertFrame(599, progress = 1f, isPlaying = false, isAtEnd = true)
    }

    @Test
    fun testSnapToThenResume() = runTest {
        launch {
            anim.animate(composition)
        }
        assertFrame(0, progress = 0f)
        assertFrame(300, progress = 0.5f)
        launch {
            anim.snapTo(composition, progress = 0.2f)
            anim.animate(composition, initialProgress = anim.progress, continueFromPreviousAnimate = true)
        }
        assertFrame(316, progress = 0.2f)
        assertFrame(449, progress = 0.422f)
        assertFrame(795, progress = 0.999f)
        assertFrame(796, progress = 1f, isPlaying = false, isAtEnd = true)
    }

    @Test
    fun testSnapToAnotherIterationThenResume() = runTest {
        launch {
            anim.animate(composition, iterations = 3)
        }
        assertFrame(0, progress = 0f, iterations = 3)
        assertFrame(1796, progress = 0.998f, iteration = 3, iterations = 3)
        launch {
            anim.snapTo(iteration = 1)
            anim.animate(composition, initialProgress = anim.progress, continueFromPreviousAnimate = true)
        }
        assertFrame(1796, progress = 0.998f, iteration = 1, iterations = 3)
        assertFrame(1797, progress = 0f, iteration = 2, iterations = 3)
        assertFrame(2994, progress = 0.998f, iteration = 3, iterations = 3)
        assertFrame(2995, progress = 1f, isPlaying = false, isAtEnd = true, iteration = 3, iterations = 3)
    }

    @Test
    fun testChangeSpeed() = runTest {
        launch {
            anim.animate(composition)
        }
        assertFrame(0, progress = 0f)
        assertFrame(300, progress = 0.5f)
        launch {
            anim.animate(composition, speed = 2f, initialProgress = anim.progress, continueFromPreviousAnimate = true)
        }
        assertFrame(316, progress = 0.554f, speed = 2f)
        assertFrame(449, progress = 0.998f, speed = 2f)
        assertFrame(450, progress = 1f, speed = 2f, isPlaying = false, isAtEnd = true)
    }

    @Test
    fun testNonCancellable() = runTest {
        val job = launch {
            anim.animate(composition, cancellationBehavior = LottieCancellationBehavior.OnIterationFinish)
        }
        assertFrame(0, progress = 0f)
        job.cancel()
        assertFrame(300, progress = 0.5f)
        assertFrame(599, progress = 1f, isAtEnd = true, isPlaying = false)
    }

    @Test
    fun testCancelWithMultipleIterations() = runTest {
        val job = launch {
            anim.animate(composition, cancellationBehavior = LottieCancellationBehavior.OnIterationFinish, iterations = 3)
        }
        assertFrame(0, progress = 0f, iterations = 3)
        job.cancel()
        assertFrame(300, progress = 0.5f, iterations = 3)
        assertFrame(599, progress = 1f, isAtEnd = false, isPlaying = false, iterations = 3)
    }

    private suspend fun assertFrame(
        frameTimeMs: Long,
        progress: Float,
        iteration: Int = 1,
        iterations: Int = 1,
        speed: Float = 1f,
        clipSpec: LottieClipSpec? = null,
        isAtEnd: Boolean = false,
        isPlaying: Boolean = true,
        lastFrameNanos: Long = frameTimeMs * 1_000_000,
    ) {
        clock.frameMs(frameTimeMs)
        assertEquals("progress at %d".format(frameTimeMs), progress, anim.progress, 0.001f)
        assertEquals("iteration at %d".format(frameTimeMs), iteration, anim.iteration)
        assertEquals("iterations at %d".format(frameTimeMs), iterations, anim.iterations)
        assertEquals("speed at %d".format(frameTimeMs), speed, anim.speed)
        assertEquals("clipSpec at %d".format(frameTimeMs), clipSpec, anim.clipSpec)
        assertEquals("isAtEnd at %d".format(frameTimeMs), isAtEnd, anim.isAtEnd)
        assertEquals("isPlaying at %d".format(frameTimeMs), isPlaying, anim.isPlaying)
        assertEquals("lastFrameNanos at %d".format(frameTimeMs), lastFrameNanos, anim.lastFrameNanos)
    }

    private fun runTest(test: suspend CoroutineScope.() -> Unit) {
        runBlockingTest {
            withContext(clock) {
                test()
            }
        }
    }
}