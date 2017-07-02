package com.airbnb.lottie.samples

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewTreeObserver
import kotlinx.android.synthetic.main.activity_typography_demo.*

class TypographyDemoActivity : AppCompatActivity() {
    private val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        scrollView.fullScroll(View.FOCUS_DOWN)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_typography_demo)
        fontView.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
    }


    override fun onDestroy() {
        fontView.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        super.onDestroy()
    }
}
