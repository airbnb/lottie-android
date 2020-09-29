package com.airbnb.lottie.samples

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.samples.databinding.TypographyDemoActivityBinding
import com.airbnb.lottie.samples.utils.viewBinding

class TypographyDemoActivity : AppCompatActivity() {
    private val binding: TypographyDemoActivityBinding by viewBinding()

    private val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        binding.scrollView.fullScroll(View.FOCUS_DOWN)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.fontView.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
    }


    override fun onDestroy() {
        binding.fontView.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        super.onDestroy()
    }
}
