package com.airbnb.lottie.issues

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.FloatRange
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.compose.LottieAnimationSpec
import com.airbnb.lottie.compose.LottieAnimationState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.renderer.ComposeLottieAnimation


class IssueReproActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val compositionResult = rememberLottieComposition(LottieAnimationSpec.RawRes(R.raw.anim))
            val state = LottieAnimationState(
                isPlaying = true,
                repeatCount = Integer.MAX_VALUE,
            )
            ComposeLottieAnimation(
                compositionResult,
                state,
            )
        }
    }
}

