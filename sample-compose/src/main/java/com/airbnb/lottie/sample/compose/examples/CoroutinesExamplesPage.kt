package com.airbnb.lottie.sample.compose.examples

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.airbnb.lottie.sample.compose.R
import kotlinx.coroutines.isActive

@Composable
fun CoroutinesExamplesPage() {
    UsageExamplePageScaffold {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.height(16.dp))
            ExampleCard("Example 1", "Repeat once") {
                Example1()
            }
            ExampleCard("Example 2", "Repeat once. Click to repeat again") {
                Example2()
            }
            ExampleCard("Example 3", "Repeat forever") {
                Example3()
            }
            ExampleCard("Example 4", "Click to toggle playback") {
                Example4()
            }
        }
    }
}


@Composable
private fun Example1() {
    val composition by lottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    val progress = remember { mutableStateOf(0f) }

    LaunchedEffect(composition) {
        animateLottieComposition(composition, progress)
    }
    LottieAnimation(composition, progress.value)
}

@Composable
private fun Example2() {
    var iteration by remember { mutableStateOf(1) }
    val composition by lottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    val progress = remember { mutableStateOf(0f) }

    LaunchedEffect(composition, iteration) {
        animateLottieComposition(composition, progress)
    }
    LottieAnimation(
        composition,
        progress.value,
        modifier = Modifier
            .clickable { iteration++ }
    )
}

@Composable
private fun Example3() {
    val composition by lottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    val progress = remember { mutableStateOf(0f) }

    LaunchedEffect(composition) {
        // Return early when the composition is parsing so it doesn't loop in animateLottieComposition
        // over and over again.
        composition ?: return@LaunchedEffect
        var lastFrameNanos: Long? = null
        while (isActive) {
            lastFrameNanos = animateLottieComposition(
                composition,
                progress,
                lastFrameTimeNanos = lastFrameNanos,
            )
        }
    }
    LottieAnimation(composition, progress.value)
}

@Composable
private fun Example4() {
    var isPlaying by remember { mutableStateOf(true) }
    val composition by lottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    val progress = remember { mutableStateOf(0f) }

    LaunchedEffect(composition, isPlaying) {
        if (composition == null || !isPlaying) return@LaunchedEffect
        var snapToMinProgress = false
        while (isActive) {
            animateLottieComposition(
                composition,
                progress,
                snapToMinProgress = snapToMinProgress,
            )
            snapToMinProgress = true
        }
    }
    LottieAnimation(
        composition,
        progress.value,
        modifier = Modifier
            .clickable { isPlaying = !isPlaying }
    )
}