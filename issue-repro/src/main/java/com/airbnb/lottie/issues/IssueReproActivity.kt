package com.airbnb.lottie.issues

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
            ) {
                ComposeLottieAnimation(
                    compositionResult,
                    state,
                    modifier = Modifier
                        .size(256.dp)
                        .background(Color.Blue)
                )
            }
        }
    }
}

