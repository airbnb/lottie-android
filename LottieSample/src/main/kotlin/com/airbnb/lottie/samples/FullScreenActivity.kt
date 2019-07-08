package com.airbnb.lottie.samples

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * To have a full screen animation, make an animation that is wider than the screen and set the
 * scaleType to centerCrop.
 */
class FullScreenActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_full_screen)
    }
}
