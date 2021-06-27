package com.airbnb.lottie.sample.compose.examples

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimatable
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.sample.compose.R

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
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    val animatable = remember { LottieAnimatable() }

    LaunchedEffect(composition, animatable) {
        animatable.animate(
            composition,
            iterations = LottieConstants.IterateForever,
        )
    }
    LottieAnimation(composition, animatable.progress)
}

@Composable
private fun Example2() {
    var nonce by remember { mutableStateOf(1) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    val animatable = remember { LottieAnimatable() }

    LaunchedEffect(composition, nonce) {
        composition ?: return@LaunchedEffect
        animatable.animate(
            composition,
            continueFromPreviousAnimate = false,
        )
    }
    LottieAnimation(
        composition,
        animatable.progress,
        modifier = Modifier
            .clickable { nonce++ }
    )
}

@Composable
private fun Example3() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    val animatable = remember { LottieAnimatable() }

    LaunchedEffect(composition, animatable) {
        composition ?: return@LaunchedEffect
        animatable.animate(
            composition,
            iterations = LottieConstants.IterateForever,
        )
    }
    LottieAnimation(composition, animatable.progress)
}

@Composable
private fun Example4() {
    var shouldPlay by remember { mutableStateOf(true) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    val animatable = remember { LottieAnimatable() }

    LaunchedEffect(composition, shouldPlay) {
        if (composition == null || !shouldPlay) return@LaunchedEffect
        animatable.animate(composition, iteration = LottieConstants.IterateForever)
    }
    LottieAnimation(
        composition,
        animatable.progress,
        modifier = Modifier
            .clickable { shouldPlay = !shouldPlay }
    )
}