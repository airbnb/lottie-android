package com.airbnb.lottie.snapshots.tests

import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.databinding.ClipChildrenBinding
import com.airbnb.lottie.snapshots.snapshotComposable

class ClipChildrenTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        val composition = LottieCompositionFactory.fromAssetSync(context, "Tests/BeyondBounds.json").value!!
        snapshotComposable("Compose Clip Children", "Clip", renderHardwareAndSoftware = true) { renderMode ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(400.dp, 400.dp)
            ) {
                LottieAnimation(
                    composition,
                    { 0.7f },
                    contentScale = ContentScale.Crop,
                    renderMode = renderMode,
                    modifier = Modifier
                        .size(200.dp, 100.dp)
                )
            }
        }

        snapshotComposable("Compose Clip Children", "Dont Clip", renderHardwareAndSoftware = true) { renderMode ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(400.dp, 400.dp)
            ) {
                LottieAnimation(
                    composition,
                    { 0.7f },
                    contentScale = ContentScale.Crop,
                    renderMode = renderMode,
                    clipToCompositionBounds = false,
                    modifier = Modifier
                        .size(200.dp, 100.dp)
                )
            }
        }

        val binding = ClipChildrenBinding.inflate(LayoutInflater.from(context))
        binding.animationView.setComposition(composition)
        binding.root.measureAndLayout()

        val bitmap1 = bitmapPool.acquire(binding.root.width, binding.root.height)
        val canvas = Canvas(bitmap1)
        binding.root.draw(canvas)
        snapshotter.record(bitmap1, "Clip Children", "Clip")

        val bitmap2 = bitmapPool.acquire(binding.root.width, binding.root.height)
        canvas.setBitmap(bitmap2)
        binding.animationView.clipToCompositionBounds = false
        binding.root.draw(canvas)
        snapshotter.record(bitmap2, "Clip Children", "Don't Clip")

    }

    private fun View.measureAndLayout() {
        val spec = View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY)
        measure(spec, spec)
        layout(0, 0, measuredWidth, measuredHeight)
    }
}