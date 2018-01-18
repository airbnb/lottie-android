package com.airbnb.lottie.samples

import android.graphics.PointF
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.utils.MiscUtils
import com.airbnb.lottie.value.LottieValueCallback
import kotlinx.android.synthetic.main.fragment_dynamic.*
import kotlinx.android.synthetic.main.fragment_dynamic.view.*

private val COLORS = arrayOf(
        0xff5a5f,
        0x008489,
        0xa61d55
)
private val EXTRA_JUMP = arrayOf(0f, 20f, 50f)
class DynamicFragment : Fragment() {
    private var speed = 1
    private var colorIndex = 0
    private var extraJumpIndex = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = container!!.inflate(R.layout.fragment_dynamic, false)

        view.speedButton.setOnClickListener {
            speed = ++speed % 4
            updateButtonText()
        }

        view.colorButton.setOnClickListener {
            colorIndex = (colorIndex + 1) % COLORS.size
            updateButtonText()
        }

        view.jumpHeight.setOnClickListener {
            extraJumpIndex = (extraJumpIndex + 1) % EXTRA_JUMP.size
            updateButtonText()
        }

        view.postDelayed({ setupValueCallbacks() }, 1000)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateButtonText()
    }

    private fun setupValueCallbacks() {
        animationView.addValueCallback(KeyPath("LeftArmWave"), LottieProperty.TIME_REMAP, object : LottieValueCallback<Float?>() {
            override fun getValue(startFrame: Float,
                                  endFrame: Float,
                                  startValue: Float?,
                                  endValue: Float?,
                                  linearKeyframeProgress: Float,
                                  interpolatedKeyframeProgress: Float,
                                  overallProgress: Float
            ): Float {
                return 2 * speed.toFloat() * overallProgress
            }
        })

        val colorCallback = object : LottieValueCallback<Int?>() {
            override fun getValue(startFrame: Float, endFrame: Float, startValue: Int?, endValue: Int?, linearKeyframeProgress: Float, interpolatedKeyframeProgress: Float, overallProgress: Float): Int? {
                return COLORS[colorIndex]
            }
        }
        animationView.addValueCallback(KeyPath("Shirt", "Group 5", "Fill 1"), LottieProperty.COLOR, colorCallback)
        animationView.addValueCallback(KeyPath("LeftArmWave", "LeftArm", "Group 6", "Fill 1"), LottieProperty.COLOR, colorCallback)
        animationView.addValueCallback(KeyPath("RightArm", "Group 6", "Fill 1"), LottieProperty.COLOR, colorCallback)
        val point = PointF()
        animationView.addValueCallback(KeyPath("Body"), LottieProperty.TRANSFORM_POSITION, object: LottieValueCallback<PointF>() {
            override fun getValue(startFrame: Float, endFrame: Float, startValue: PointF, endValue: PointF, linearKeyframeProgress: Float, interpolatedKeyframeProgress: Float, overallProgress: Float): PointF {
                var startY = startValue.y
                var endY = endValue.y

                if (startY == 140.29837f) {
                    startY += EXTRA_JUMP[extraJumpIndex]
                }
                if (endY == 140.29837f) {
                    endY += EXTRA_JUMP[extraJumpIndex]
                }
                point.set(startValue.x, MiscUtils.lerp(startY, endY, interpolatedKeyframeProgress))

                return point
            }
        })
    }

    private fun updateButtonText() {
        speedButton.text = "Wave: ${speed}x Speed"
        jumpHeight.text = "Extra jump height ${EXTRA_JUMP[extraJumpIndex]}"
    }
}
