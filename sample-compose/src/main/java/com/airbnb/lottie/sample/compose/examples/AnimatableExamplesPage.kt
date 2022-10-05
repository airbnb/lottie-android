package com.airbnb.lottie.sample.compose.examples

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.sample.compose.R

@Composable
fun AnimatableExamplesPage() {
    UsageExamplePageScaffold {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            ExampleCard("Example 1", "Repeat Forever") {
                Example1()
            }
            ExampleCard("Example 2", "Draggable Progress Slider") {
                Example2()
            }
            ExampleCard("Example 3", "Draggable Speed Slider") {
                Example3()
            }
            ExampleCard("Example 4", "Repeat once. Click to repeat again") {
                Example4()
            }
            ExampleCard("Example 5", "Click to toggle playback") {
                Example5()
            }
            ExampleCard("Example 6", "Reverse Animation on Repeat") {
                Example6()
            }
        }
    }
}

@Composable
private fun Example1() {
    val anim = rememberLottieAnimatable()
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    LaunchedEffect(composition) {
        anim.animate(
            composition,
            iterations = LottieConstants.IterateForever,
        )
    }
    LottieAnimation(anim.composition, { anim.progress })
}

@Composable
private fun Example2() {
    val anim = rememberLottieAnimatable()
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    var sliderGestureProgress: Float? by remember { mutableStateOf(null) }
    LaunchedEffect(composition, sliderGestureProgress) {
        when (val p = sliderGestureProgress) {
            null -> anim.animate(
                composition,
                iterations = LottieConstants.IterateForever,
                initialProgress = anim.progress,
                continueFromPreviousAnimate = false,
            )
            else -> anim.snapTo(progress = p)
        }
    }
    Box {
        LottieAnimation(anim.composition, { anim.progress })
        Slider(
            value = sliderGestureProgress ?: anim.progress,
            onValueChange = { sliderGestureProgress = it },
            onValueChangeFinished = { sliderGestureProgress = null },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun Example3() {
    val anim = rememberLottieAnimatable()
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    var speed by remember { mutableStateOf(1f) }
    LaunchedEffect(composition, speed) {
        anim.animate(
            composition,
            iterations = LottieConstants.IterateForever,
            speed = speed,
            initialProgress = anim.progress,
        )
    }
    Box {
        LottieAnimation(composition, { anim.progress })
        Slider(
            value = speed,
            onValueChange = { speed = it },
            valueRange = -3f..3f,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .size(width = 1.dp, height = 16.dp)
                .background(Color.Black)
        )
    }
}

@Composable
private fun Example4() {
    var nonce by remember { mutableStateOf(1) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    val animatable = rememberLottieAnimatable()

    LaunchedEffect(composition, nonce) {
        composition ?: return@LaunchedEffect
        animatable.animate(
            composition,
            continueFromPreviousAnimate = false,
        )
    }
    LottieAnimation(
        composition,
        { animatable.progress },
        modifier = Modifier
            .clickable { nonce++ }
    )
}

@Composable
private fun Example5() {
    var shouldPlay by remember { mutableStateOf(true) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    val animatable = rememberLottieAnimatable()

    LaunchedEffect(composition, shouldPlay) {
        if (composition == null || !shouldPlay) return@LaunchedEffect
        animatable.animate(composition, iteration = LottieConstants.IterateForever)
    }
    LottieAnimation(
        composition,
        { animatable.progress },
        modifier = Modifier
            .clickable { shouldPlay = !shouldPlay }
    )
}

@Composable
private fun Example6() {
    val anim = rememberLottieAnimatable()
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    LaunchedEffect(composition) {
        anim.animate(
            composition,
            iterations = LottieConstants.IterateForever,
            reverseOnRepeat = true,
        )
    }
    LottieAnimation(anim.composition, { anim.progress })
}

