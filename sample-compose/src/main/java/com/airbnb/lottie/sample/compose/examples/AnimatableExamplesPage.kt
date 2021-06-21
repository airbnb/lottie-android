package com.airbnb.lottie.sample.compose.examples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Slider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
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
        }
    }
}

@Composable
private fun Example1() {
    val anim = remember { LottieAnimatable() }
    val composition by lottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    LaunchedEffect(composition) {
        anim.animate(
            composition,
            iterations = LottieConstants.IterateForever,
        )
    }
    LottieAnimation(composition, anim.progress)
}

@Composable
private fun Example2() {
    val anim = remember { LottieAnimatable() }
    val composition by lottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
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
        LottieAnimation(composition, anim.progress)
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
    val anim = remember { LottieAnimatable() }
    val composition by lottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
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
        LottieAnimation(composition, anim.progress)
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