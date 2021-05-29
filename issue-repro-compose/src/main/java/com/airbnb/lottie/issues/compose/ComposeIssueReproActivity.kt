package com.airbnb.lottie.issues.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.isActive

class ComposeIssueReproActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Content()
        }
    }

    @Composable
    fun Content() {
        var state by remember { mutableStateOf(0) }
        val compositionResult = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))

        val progress by lottieTransition(state) { progress ->
            val composition = compositionResult.await()
            when (state) {
                0 -> animateLottieComposition(
                    composition,
                    progress,
                    clipSpec = LottieAnimationClipSpec.MinAndMaxProgress(0f, 0.25f),
                    cancellationBehavior = LottieCancellationBehavior.AtEnd,
                )
                1 -> {
                    while (isActive) {
                        animateLottieComposition(
                            composition,
                            progress,
                            clipSpec = LottieAnimationClipSpec.MinAndMaxProgress(0.25f, 0.75f),
                            cancellationBehavior = LottieCancellationBehavior.AtEnd,
                        )
                    }
                }
                else -> {
                    animateLottieComposition(
                        composition,
                        progress,
                        clipSpec = LottieAnimationClipSpec.MinAndMaxProgress(0.75f, 1f),
                        cancellationBehavior = LottieCancellationBehavior.AtEnd,
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LottieAnimation(
                compositionResult(),
                progress,
            )
            TextButton(
                onClick = { state = (state + 1) % 3 },
            ) {
                Text(state.toString())
            }
        }
    }
}
