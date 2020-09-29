package com.airbnb.lottie.samples

import android.graphics.PointF
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.customview.widget.ViewDragHelper
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.samples.databinding.BullseyeActivityBinding
import com.airbnb.lottie.samples.utils.viewBinding
import com.airbnb.lottie.value.LottieRelativePointValueCallback

class BullseyeActivity : AppCompatActivity() {
    private val binding: BullseyeActivityBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val largeValueCallback = LottieRelativePointValueCallback(PointF(0f, 0f))
        binding.animationView.addValueCallback(KeyPath("First"), LottieProperty.TRANSFORM_POSITION, largeValueCallback)

        val mediumValueCallback = LottieRelativePointValueCallback(PointF(0f, 0f))
        binding.animationView.addValueCallback(KeyPath("Fourth"), LottieProperty.TRANSFORM_POSITION, mediumValueCallback)

        val smallValueCallback = LottieRelativePointValueCallback(PointF(0f, 0f))
        binding.animationView.addValueCallback(KeyPath("Seventh"), LottieProperty.TRANSFORM_POSITION, smallValueCallback)

        var totalDx = 0f
        var totalDy = 0f

        val viewDragHelper = ViewDragHelper.create(binding.containerView, object : ViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int) = child == binding.targetView

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
            }
        })

        binding.containerView.viewDragHelper = viewDragHelper
    }

    private fun getPoint(dx: Float, dy: Float, factor: Float) = PointF(dx * factor, dy * factor)
}