package com.airbnb.lottie.issues.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

class ComposeIssueReproActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Content()
        }
    }

    @Composable
    fun Content() {
        val states = remember { MutableStateFlow(0) }
        val state by states.collectAsState()
        val progress = remember { mutableStateOf(0f) }

        val compositionResult = rememberLottieCompositionResult(LottieCompositionSpec.RawRes(R.raw.loading))

        LaunchedEffect(Unit) {
            val composition = compositionResult.await()
            states.collectLatest { state ->
                when (state) {
                    0 -> {
                        animateLottieComposition(
                            composition,
                            progress,
                            clipSpec = LottieAnimationClipSpec.MinAndMaxProgress(0f, 0.25f),
                            cancellationBehavior = LottieCancellationBehavior.AtEnd,
                            )
                    }
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
        }

        Column {
            LottieAnimation(
                compositionResult(),
                progress.value,
            )
            TextButton(
                onClick = { states.value = (state + 1) % 3 },
            ) {
                Text(state.toString())
            }
        }
    }
}
