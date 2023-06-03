package com.airbnb.lottie.issues

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.L
import com.airbnb.lottie.issues.databinding.IssueReproActivityBinding

class IssueReproActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = IssueReproActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Reproduce any issues here.
        binding.animationView.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationRepeat(animation: Animator) {
                Log.d("Gabe", "Draw %.1f".format(
                    L.drawTimeNs.getAndSet(0L) / 1_000_000f,
                ))
            }
        })

    }
}
