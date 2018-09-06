package com.airbnb.lottie.samples

import android.app.Activity
import android.os.Bundle
import com.airbnb.lottie.LottieAnimationView

class DebugActivity : Activity() {
    private val lottieView by lazy { findViewById<LottieAnimationView>(R.id.lottie_animation_view) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        lottieView.imageAssetsFolder = "Images/WeAccept"
        lottieView.setAnimation("Tests/WeAccept.json")
        lottieView.playAnimation()
    }
}