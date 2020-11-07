package com.airbnb.lottie.samples

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.samples.databinding.EmptyActivityBinding
import com.airbnb.lottie.samples.utils.viewBinding

class EmptyActivity : AppCompatActivity() {
    private val binding: EmptyActivityBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.finish.setOnClickListener { finish() }
    }
}
