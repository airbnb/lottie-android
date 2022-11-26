package com.airbnb.lottie.snapshots.tests

import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View.MeasureSpec
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.model.LottieCompositionCache
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.log
import com.airbnb.lottie.snapshots.databinding.SeekBarBinding

class SeekBarTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        val composition = LottieCompositionFactory.fromAssetSync(context, "Tests/Thumb.json").value!!
        val drawable = LottieDrawable()
        drawable.composition = composition
        val binding = SeekBarBinding.inflate(LayoutInflater.from(context))
        binding.seekBar.thumb = drawable

        val widthSpec = MeasureSpec.makeMeasureSpec(512, MeasureSpec.EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        binding.root.measure(widthSpec, heightSpec)
        binding.root.layout(0, 0, binding.root.measuredWidth, binding.root.measuredHeight)
        log("Drawing seek bar ${binding.root.measuredWidth}x${binding.root.measuredHeight} -> ${binding.root.width}x${binding.root.height}")
        val bitmap = bitmapPool.acquire(binding.root.measuredWidth, binding.root.measuredHeight)
        val canvas = Canvas(bitmap)
        binding.root.draw(canvas)
        snapshotter.record(bitmap, "SeekBar", "ThumbDrawable")
        LottieCompositionCache.getInstance().clear()
        bitmapPool.release(bitmap)
    }
}