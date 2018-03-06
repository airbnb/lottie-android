package com.airbnb.lottie.samples

import android.graphics.PointF
import android.os.Bundle
import android.support.v4.widget.ViewDragHelper
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieRelativeFloatValueCallback
import com.airbnb.lottie.value.LottieRelativePointValueCallback
import kotlinx.android.synthetic.main.activity_bullseye.*

class BullseyeActivity : AppCompatActivity() {

    private var firstKeypath: KeyPath? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bullseye)

        val largeValueCallback = LottieRelativePointValueCallback(PointF(0f, 0f))
        animationView.addValueCallback(KeyPath("First"), LottieProperty.TRANSFORM_POSITION, largeValueCallback)

        val mediumValueCallback = LottieRelativePointValueCallback(PointF(0f, 0f))
        animationView.addValueCallback(KeyPath("Fourth"), LottieProperty.TRANSFORM_POSITION, mediumValueCallback)

        val smallValueCallback = LottieRelativePointValueCallback(PointF(0f, 0f))
        animationView.addValueCallback(KeyPath("Seventh"), LottieProperty.TRANSFORM_POSITION, smallValueCallback)

        val bugRotationCallback = LottieRelativeFloatValueCallback(0f)
        animationView.addValueCallback(KeyPath("Bug"), LottieProperty.TRANSFORM_ROTATION, bugRotationCallback)

        var totalDx = 0f
        var totalDy = 0f

        val viewDragHelper = ViewDragHelper.create(containerView, object : ViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int) = child == targetView

            override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
                return top
            }

            override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
                return left
            }

            override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
                totalDx += dx
                totalDy += dy
                smallValueCallback.setValue(getPoint(totalDx, totalDy, 1.2f))
                mediumValueCallback.setValue(getPoint(totalDx, totalDy, 1f))
                largeValueCallback.setValue(getPoint(totalDx, totalDy, 0.75f))

                bugRotationCallback.setValue(totalDx)
            }
        })

        containerView.viewDragHelper = viewDragHelper
    }

    private fun getPoint(dx: Float, dy: Float, factor: Float) = PointF(dx * factor, dy * factor)
}