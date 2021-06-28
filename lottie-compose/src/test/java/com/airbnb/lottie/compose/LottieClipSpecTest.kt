package com.airbnb.lottie.compose

import android.graphics.Rect
import androidx.collection.LongSparseArray
import androidx.collection.SparseArrayCompat
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.model.Marker
import org.junit.Assert.*
import org.junit.Test

class LottieClipSpecTest {

    @Test
    fun testMinFrame() {
        val spec = LottieClipSpec.Frame(min = 20)
        val composition = createComposition(endFrame = 40f)
        assertEquals(0.5f, spec.getMinProgress(composition))
        assertEquals(1f, spec.getMaxProgress(composition))
    }

    @Test
    fun testMaxFrame() {
        val spec = LottieClipSpec.Frame(max = 20)
        val composition = createComposition(endFrame = 40f)
        assertEquals(0f, spec.getMinProgress(composition))
        assertEquals(0.5f, spec.getMaxProgress(composition))
    }

    @Test
    fun testMaxFrameNotInclusive() {
        val spec = LottieClipSpec.Frame(max = 20, maxInclusive = false)
        val composition = createComposition(endFrame = 40f)
        assertEquals(0f, spec.getMinProgress(composition))
        assertEquals(0.475f, spec.getMaxProgress(composition))
    }

    @Test
    fun testMinAndMaxFrame() {
        val spec = LottieClipSpec.Frame(min = 20, max = 30)
        val composition = createComposition(endFrame = 40f)
        assertEquals(0.5f, spec.getMinProgress(composition))
        assertEquals(0.75f, spec.getMaxProgress(composition))
    }

    @Test
    fun testMinAndMaxFrameNotExclusive() {
        val spec = LottieClipSpec.Frame(min = 20, max = 30, maxInclusive = false)
        val composition = createComposition(endFrame = 40f)
        assertEquals(0.5f, spec.getMinProgress(composition))
        assertEquals(0.725f, spec.getMaxProgress(composition))
    }

    @Test
    fun testMinProgress() {
        val spec = LottieClipSpec.Progress(min = 0.5f)
        val composition = createComposition(endFrame = 40f)
        assertEquals(0.5f, spec.getMinProgress(composition))
        assertEquals(1f, spec.getMaxProgress(composition))
    }

    @Test
    fun testMaxProgress() {
        val spec = LottieClipSpec.Progress(max = 0.5f)
        val composition = createComposition(endFrame = 40f)
        assertEquals(0f, spec.getMinProgress(composition))
        assertEquals(0.5f, spec.getMaxProgress(composition))
    }

    @Test
    fun testMinAndMaxProgress() {
        val spec = LottieClipSpec.Progress(min = 0.5f, max = 0.75f)
        val composition = createComposition(endFrame = 40f)
        assertEquals(0.5f, spec.getMinProgress(composition))
        assertEquals(0.75f, spec.getMaxProgress(composition))
    }

    @Test
    fun testMinMarker() {
        val spec = LottieClipSpec.Markers(min = "start")
        val composition = createComposition(endFrame = 40f, listOf(Marker("start", 20f, 10f)))
        assertEquals(0.5f, spec.getMinProgress(composition))
        assertEquals(1f, spec.getMaxProgress(composition))
    }

    @Test
    fun testMaxMarker() {
        val spec = LottieClipSpec.Markers(max = "end")
        val composition = createComposition(endFrame = 40f, listOf(Marker("end", 20f, 10f)))
        assertEquals(0f, spec.getMinProgress(composition))
        assertEquals(0.5f, spec.getMaxProgress(composition))
    }


    @Test
    fun testMaxMarkerExclusive() {
        val spec = LottieClipSpec.Markers(max = "end", maxInclusive = false)
        val composition = createComposition(endFrame = 40f, listOf(Marker("end", 20f, 10f)))
        assertEquals(0f, spec.getMinProgress(composition))
        assertEquals(0.475f, spec.getMaxProgress(composition))
    }


    @Test
    fun testMinAndMaxMarker() {
        val spec = LottieClipSpec.Markers(min = "start", max = "end")
        val composition = createComposition(endFrame = 40f, listOf(Marker("start", 20f, 10f), Marker("end", 30f, 10f)))
        assertEquals(0.5f, spec.getMinProgress(composition))
        assertEquals(0.75f, spec.getMaxProgress(composition))
    }

    @Test
    fun testMinAndMaxMarkerExclusive() {
        val spec = LottieClipSpec.Markers(min = "start", max = "end", maxInclusive = false)
        val composition = createComposition(endFrame = 40f, listOf(Marker("start", 20f, 10f), Marker("end", 30f, 10f)))
        assertEquals(0.5f, spec.getMinProgress(composition))
        assertEquals(0.725f, spec.getMaxProgress(composition))
    }

    @Test
    fun testMarker() {
        val spec = LottieClipSpec.Marker("span")
        val composition = createComposition(endFrame = 40f, listOf(Marker("span", 20f, 10f)))
        assertEquals(0.5f, spec.getMinProgress(composition))
        assertEquals(0.75f, spec.getMaxProgress(composition))
    }

    private fun createComposition(endFrame: Float, markers: List<Marker> = emptyList()): LottieComposition {
        val composition = LottieComposition()
        composition.init(
            Rect(),
            0f,
            endFrame,
            30f,
            emptyList(),
            LongSparseArray(),
            emptyMap(),
            emptyMap(),
            SparseArrayCompat(),
            emptyMap(),
            markers,
        )
        return composition
    }
}