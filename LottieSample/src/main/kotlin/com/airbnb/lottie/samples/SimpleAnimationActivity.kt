package com.airbnb.lottie.samples

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.model.LottieCompositionCache
import kotlinx.android.synthetic.main.activity_simple_animation.*
import java.lang.IllegalArgumentException

/**
 * Useful for performance debugging.
 * adb shell am start -n com.airbnb.lottie/.samples.SimpleAnimationActivity --es animation LottieLogo1.json --activity-clear-top
 */
class SimpleAnimationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_animation)
        var composition: LottieComposition? = null
        parse.setOnClickListener {
            val assetName = intent.extras?.getString("animation") ?: ""
            LottieCompositionCache.getInstance().clear()
            val start = System.currentTimeMillis()
            composition = LottieCompositionFactory.fromAssetSync(this, assetName).value
                    ?: throw IllegalArgumentException("Invalid composition $assetName")
            Toast.makeText(this@SimpleAnimationActivity, "Done ${System.currentTimeMillis() - start}", Toast.LENGTH_SHORT).show()
        }

        setComposition.setOnClickListener {
            val start = System.currentTimeMillis()
            val drawable = LottieDrawable()
            drawable.setComposition(composition)
            Toast.makeText(this@SimpleAnimationActivity, "Done ${System.currentTimeMillis() - start}", Toast.LENGTH_SHORT).show()
        }

        play.setOnClickListener {
            composition?.let { animationView.setComposition(it) }
            animationView.playAnimation()
        }
    }
}