package com.airbnb.lottie.issues.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

class ComposeIssueReproActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Content()
        }
    }

    @Composable
    fun Content() {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.rect))
        val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
        val state = rememberScrollState()
        Column(
            Modifier.verticalScroll(state)
        ) {
            repeat(10) {
                Text("${it}")
            }
            LottieAnimation(composition, { progress })
            repeat(10) {
                Text("${it}")
            }
        }
    }
}
