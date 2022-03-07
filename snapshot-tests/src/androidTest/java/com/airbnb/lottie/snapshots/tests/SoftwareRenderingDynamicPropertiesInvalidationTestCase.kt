package com.airbnb.lottie.snapshots.tests

import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.snapshots.R
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.value.LottieFrameInfo
import com.airbnb.lottie.value.LottieValueCallback

/**
 * When using software rendering, Lottie caches its internal render bitmap if the animation changes.
 * However, if a dynamic property changes in a LottieValueCallback, the consumer must call LottieAnimationView.invalidate()
 * or LottieDrawable.invalidateSelf() to invalidate the drawing cache.
 */
class SoftwareRenderingDynamicPropertiesInvalidationTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        val animationView = animationViewPool.acquire()
        val composition = LottieCompositionFactory.fromRawResSync(context, R.raw.heart).value!!
        animationView.setComposition(composition)
        animationView.renderMode = RenderMode.SOFTWARE
        animationView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        animationView.scaleType = ImageView.ScaleType.FIT_CENTER
        val widthSpec = View.MeasureSpec.makeMeasureSpec(
            context.resources.displayMetrics.widthPixels,
            View.MeasureSpec.EXACTLY,
        )
        val heightSpec = View.MeasureSpec.makeMeasureSpec(
            context.resources.displayMetrics.heightPixels,
            View.MeasureSpec.EXACTLY,
        )
        val animationViewContainer = animationView.parent as ViewGroup
        animationViewContainer.measure(widthSpec, heightSpec)
        animationViewContainer.layout(0, 0, animationViewContainer.measuredWidth, animationViewContainer.measuredHeight)
        val canvas = Canvas()

        var color = Color.GREEN
        animationView.addValueCallback(KeyPath("**", "Fill 1"), LottieProperty.COLOR, object : LottieValueCallback<Int>() {
            override fun getValue(frameInfo: LottieFrameInfo<Int>?): Int {
                return color
            }
        })

        var bitmap = bitmapPool.acquire(animationView.width, animationView.height)
        canvas.setBitmap(bitmap)
        animationView.draw(canvas)
        snapshotter.record(bitmap, "Heart Software Dynamic Property", "Green")
        bitmapPool.release(bitmap)

        bitmap = bitmapPool.acquire(animationView.width, animationView.height)
        canvas.setBitmap(bitmap)
        color = Color.BLUE
        animationView.invalidate()
        animationView.draw(canvas)
        snapshotter.record(bitmap, "Heart Software Dynamic Property", "Blue")
        bitmapPool.release(bitmap)

        animationViewPool.release(animationView)
    }
}