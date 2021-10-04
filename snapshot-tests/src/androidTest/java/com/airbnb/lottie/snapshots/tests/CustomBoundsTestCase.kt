package com.airbnb.lottie.snapshots.tests

import android.graphics.Canvas
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.model.LottieCompositionCache
import com.airbnb.lottie.snapshots.R
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CustomBoundsTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        val composition = LottieCompositionFactory.fromRawResSync(context, R.raw.heart).value!!
        val bitmap = bitmapPool.acquire(50, 100)
        val canvas = Canvas(bitmap)
        val drawable = LottieDrawable()
        drawable.composition = composition
        drawable.repeatCount = Integer.MAX_VALUE
        drawable.setBounds(0, 0, 25, 100)
        withContext(Dispatchers.Main) {
            drawable.draw(canvas)
        }
        LottieCompositionCache.getInstance().clear()
        snapshotter.record(bitmap, "CustomBounds", "Heart-25x100")
        bitmapPool.release(bitmap)
    }
}