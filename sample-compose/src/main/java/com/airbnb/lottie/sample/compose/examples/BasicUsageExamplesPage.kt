package com.airbnb.lottie.sample.compose.examples

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.sample.compose.R

@Composable
fun BasicUsageExamplesPage() {
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
            ExampleCard("Example 2", "Repeat forever") {
                Example2()
            }
            ExampleCard("Example 3", "Repeat forever from 50% to 75%") {
                Example3()
            }
            ExampleCard("Example 4", "Using LottieAnimationResult") {
                Example4()
            }
            ExampleCard("Example 5", "Using LottieComposition") {
                Example5()
            }
            ExampleCard("Example 6", "Splitting out the animation driver") {
                Example6()
            }
            ExampleCard("Example 7", "Toggle on click - click me") {
                Example7()
            }
        }
    }
}

/**
 * Nice and easy... This will play one time as soon as the composition loads
 * then it will stop.
 */
@Composable
private fun Example1() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    LottieAnimation(composition)
}

/**
 * This will repeat forever.
 */
@Composable
private fun Example2() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    LottieAnimation(
        composition,
        iterations = LottieConstants.IterateForever,
    )
}

/**
 * This will repeat between 50% and 75% forever.
 */
@Composable
private fun Example3() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    LottieAnimation(
        composition,
        iterations = LottieConstants.IterateForever,
        clipSpec = LottieClipSpec.Progress(0.5f, 0.75f),
    )
}

/**
 * Here, you can check the result for loading/failure states.
 */
@Composable
private fun Example4() {
    val compositionResult = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    when {
        compositionResult.isLoading -> {
            Text("Animation is loading...")
        }
        compositionResult.isFailure -> {
            Text("Animation failed to load")
        }
        compositionResult.isSuccess -> {
            LottieAnimation(
                compositionResult.value,
                iterations = LottieConstants.IterateForever,
            )
        }
    }
}

/**
 * If you just want access to the composition itself, you can use the delegate
 * version of lottieComposition like this.
 */
@Composable
private fun Example5() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    LottieAnimation(
        composition,
        progress = 0.65f,
    )
}

/**
 * Here, you have access to the composition and animation individually.
 */
@Composable
private fun Example6() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
    )
    LottieAnimation(
        composition,
        { progress },
    )
}

/**
 * Here, you can toggle playback by clicking the animation.
 */
@Composable
private fun Example7() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    var isPlaying by remember { mutableStateOf(false) }
    LottieAnimation(
        composition,
        iterations = LottieConstants.IterateForever,
        // When this is true, it it will start from 0 every time it is played again.
        // When this is false, it will resume from the progress it was pause at.
        restartOnPlay = false,
        isPlaying = isPlaying,
        modifier = Modifier
            .clickable { isPlaying = !isPlaying }
    )
}

@Preview
@Composable
fun ExampleCardPreview() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    ExampleCard("Example 1", "Heart animation") {
        LottieAnimation(composition)
    }
}