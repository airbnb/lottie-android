package com.airbnb.lottie.tests.espresso

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class EmptyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empty)
        findViewById<View>(R.id.finish).setOnClickListener { finish() }
    }
}
