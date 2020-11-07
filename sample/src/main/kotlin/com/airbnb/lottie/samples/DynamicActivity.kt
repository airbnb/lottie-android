package com.airbnb.lottie.samples

import android.annotation.SuppressLint
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.samples.databinding.DynamicActivityBinding
import com.airbnb.lottie.samples.utils.viewBinding
import com.airbnb.lottie.utils.MiscUtils

private val COLORS = arrayOf(
    0xff5a5f,
    0x008489,
    0xa61d55
)
private val EXTRA_JUMP = arrayOf(0f, 20f, 50f)

class DynamicActivity : AppCompatActivity() {
    private val binding: DynamicActivityBinding by viewBinding()

    private var speed = 1
    private var colorIndex = 0
    private var extraJumpIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.speedButton.setOnClickListener {
            speed = ++speed % 4
            updateButtonText()
        }

        binding.colorButton.setOnClickListener {
            colorIndex = (colorIndex + 1) % COLORS.size
            updateButtonText()
        }

        binding.jumpHeight.setOnClickListener {
            extraJumpIndex = (extraJumpIndex + 1) % EXTRA_JUMP.size
            updateButtonText()
            setupValueCallbacks()
        }

        binding.animationView.addLottieOnCompositionLoadedListener { _ ->
            binding.animationView.resolveKeyPath(KeyPath("**")).forEach {
                Log.d(TAG, it.keysToString())
                setupValueCallbacks()
            }
        }
        binding.animationView.setFailureListener { e ->
            Log.e(TAG, "Failed to load animation!", e)
        }

        updateButtonText()
    }

    private fun setupValueCallbacks() {
        binding.animationView.addValueCallback(KeyPath("LeftArmWave"), LottieProperty.TIME_REMAP) { frameInfo ->
            2 * speed.toFloat() * frameInfo.overallProgress
        }

        val shirt = KeyPath("Shirt", "Group 5", "Fill 1")
        val leftArm = KeyPath("LeftArmWave", "LeftArm", "Group 6", "Fill 1")
        val rightArm = KeyPath("RightArm", "Group 6", "Fill 1")

        binding.animationView.addValueCallback(shirt, LottieProperty.COLOR) { COLORS[colorIndex] }
        binding.animationView.addValueCallback(leftArm, LottieProperty.COLOR) { COLORS[colorIndex] }
        binding.animationView.addValueCallback(rightArm, LottieProperty.COLOR) { COLORS[colorIndex] }
        val point = PointF()
        binding.animationView.addValueCallback(KeyPath("Body"),
            LottieProperty.TRANSFORM_POSITION) { frameInfo ->
            val startX = frameInfo.startValue.x
            var startY = frameInfo.startValue.y
            var endY = frameInfo.endValue.y

            if (startY > endY) {
                startY += EXTRA_JUMP[extraJumpIndex]
            } else if (endY > startY) {
                endY += EXTRA_JUMP[extraJumpIndex]
            }
            point.set(startX, MiscUtils.lerp(startY, endY, frameInfo.interpolatedKeyframeProgress))
            point
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateButtonText() {
        binding.speedButton.text = "Wave: ${speed}x Speed"
        binding.jumpHeight.text = "Extra jump height ${EXTRA_JUMP[extraJumpIndex]}"
    }

    companion object {
        val TAG = DynamicActivity::class.simpleName
    }
}
