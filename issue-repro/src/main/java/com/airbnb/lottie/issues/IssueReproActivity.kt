package com.airbnb.lottie.issues

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.issues.databinding.IssueReproActivityBinding

class IssueReproActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = IssueReproActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val maxProgress = 0.32f

        with(binding.animationView) {

            setMaxProgress(maxProgress)

            addAnimatorUpdateListener {
                Log.d(TAG, "AnimatorUpdateListener progress = $progress targetProgress = $maxProgress")
            }

            postDelayed(
                {
                    if (progress > maxProgress) {
                        throw IllegalStateException(
                            "View's progress($progress) is greater than MAX progress($maxProgress) " +
                                    "Expected: Progress would not exceed maxProgress value. " +
                                    "Actual: Progress is greater than maxProgress value."
                        )
                    }
                },
                ANIMATION_DURATION_TIME_MS
            )

            playAnimation()
        }


    }

    companion object {
        const val TAG = "IssueReproActivity"
        const val ANIMATION_DURATION_TIME_MS = 5000L
    }
}
