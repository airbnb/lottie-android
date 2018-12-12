package com.airbnb.lottie.samples

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_simple_animation.*

class SimpleAnimationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_animation)
        animationView.setAnimation(intent.extras?.getString("animation") ?: "")
    }
}