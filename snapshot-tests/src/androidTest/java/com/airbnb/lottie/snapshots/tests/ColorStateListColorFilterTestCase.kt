package com.airbnb.lottie.snapshots.tests

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import com.airbnb.lottie.model.LottieCompositionCache
import com.airbnb.lottie.snapshots.R
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.databinding.TestColorFilterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class ColorStateListColorFilterTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        val context = ContextThemeWrapper(context, R.style.Theme_LottieCompose)
        val binding = TestColorFilterBinding.inflate(LayoutInflater.from(context))
        withTimeout(10_000) {
            while (binding.animationView.composition == null) {
                delay(50)
            }
        }

        val bitmap = bitmapPool.acquire(1000, 1000)
        val canvas = Canvas(bitmap)
        val spec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY)
        binding.root.measure(spec, spec)
        binding.root.layout(0, 0, 1000, 1000)
        canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR)
        withContext(Dispatchers.Main) {
            binding.root.draw(canvas)
        }
        LottieCompositionCache.getInstance().clear()
        snapshotter.record(bitmap, "ColorFilter", "ColorStateList")
        bitmapPool.release(bitmap)
    }
}