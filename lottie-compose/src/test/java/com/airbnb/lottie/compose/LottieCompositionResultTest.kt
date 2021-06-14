package com.airbnb.lottie.compose

import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LottieCompositionResultTest {

    private lateinit var composition: LottieComposition

    @Before
    fun setup() {
        composition = LottieCompositionFactory.fromJsonStringSync(CompositionFixtures.Rect, null).value!!
    }

    @Test
    fun testLoading() {
        val result = LottieCompositionResult()
        assertTrue(result.isLoading)
    }

    @Test
    fun testFail() {
        val result = LottieCompositionResult()
        val e = IllegalStateException("Fail")
        result.completeExceptionally(e)
        assertFalse(result.isSuccess)
        assertTrue(result.isFailure)
        assertNull(result.value)
        assertEquals(e, result.error)
    }

    @Test
    fun testCompleted() {
        val result = LottieCompositionResult()
        result.complete(composition)
        assertFalse(result.isFailure)
        assertTrue(result.isSuccess)
        assertEquals(composition, result.value)
    }

    @Test
    fun testCompletedThenFail() {
        val result = LottieCompositionResult()
        result.complete(composition)
        result.completeExceptionally(IllegalStateException("Fail"))
        assertFalse(result.isFailure)
        assertTrue(result.isSuccess)
        assertEquals(composition, result.value)
    }

    @Test
    fun testErrorThenCompleted() {
        val result = LottieCompositionResult()
        val e = IllegalStateException("Fail")
        result.completeExceptionally(e)
        result.complete(composition)
        assertFalse(result.isSuccess)
        assertTrue(result.isFailure)
        assertNull(result.value)
        assertEquals(e, result.error)
    }
}