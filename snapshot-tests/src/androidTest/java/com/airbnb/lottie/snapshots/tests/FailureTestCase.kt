package com.airbnb.lottie.snapshots.tests

import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.airbnb.lottie.snapshots.R
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.utils.SuspendingSemaphore

class FailureTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        val animationView = animationViewPool.acquire()
        val semaphore = SuspendingSemaphore(0)
        animationView.setFailureListener { semaphore.release() }
        animationView.setFallbackResource(R.drawable.ic_close)
        animationView.setAnimationFromJson("Not Valid Json", null)
        semaphore.acquire()
        animationView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        animationView.scale = 1f
        animationView.scaleType = ImageView.ScaleType.FIT_CENTER
        val widthSpec = View.MeasureSpec.makeMeasureSpec(
            context.resources.displayMetrics
                .widthPixels,
            View.MeasureSpec.EXACTLY
        )
        val heightSpec = View.MeasureSpec.makeMeasureSpec(
            context.resources.displayMetrics
                .heightPixels, View.MeasureSpec.EXACTLY
        )
        val animationViewContainer = animationView.parent as ViewGroup
        animationViewContainer.measure(widthSpec, heightSpec)
        animationViewContainer.layout(0, 0, animationViewContainer.measuredWidth, animationViewContainer.measuredHeight)
        val bitmap = bitmapPool.acquire(animationView.width, animationView.height)
        val canvas = Canvas(bitmap)
        animationView.draw(canvas)
        animationViewPool.release(animationView)
        val snapshotName = "Failure"
        val snapshotVariant = "Default"
        snapshotter.record(bitmap, snapshotName, snapshotVariant)
        bitmapPool.release(bitmap)
    }
}