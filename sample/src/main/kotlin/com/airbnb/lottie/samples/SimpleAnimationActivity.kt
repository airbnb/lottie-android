package com.airbnb.lottie.samples

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.samples.databinding.SimpleAnimationActivityBinding
import com.airbnb.lottie.samples.utils.viewBinding

/**
 * Useful for performance debugging.
 * adb shell am start -n com.airbnb.lottie/.samples.SimpleAnimationActivity --es animation LottieLogo1.json --activity-clear-top
 */
class SimpleAnimationActivity : AppCompatActivity() {
    private val binding: SimpleAnimationActivityBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var composition: LottieComposition? = null
        binding.parse.setOnClickListener {
            val assetName = intent.extras?.getString("animation") ?: ""
            val start = System.currentTimeMillis()
            composition = LottieCompositionFactory.fromAssetSync(this, assetName, null).value
                    ?: throw IllegalArgumentException("Invalid composition $assetName")
            Toast.makeText(this@SimpleAnimationActivity, "Done ${System.currentTimeMillis() - start}", Toast.LENGTH_SHORT).show()
        }

        binding.setComposition.setOnClickListener {
            val start = System.currentTimeMillis()
            val drawable = LottieDrawable()
            drawable.setComposition(composition)
            Toast.makeText(this@SimpleAnimationActivity, "Done ${System.currentTimeMillis() - start}", Toast.LENGTH_SHORT).show()
        }

        binding.play.setOnClickListener {
            composition?.let { binding.animationView.setComposition(it) }
            binding.animationView.playAnimation()
        }
    }
}