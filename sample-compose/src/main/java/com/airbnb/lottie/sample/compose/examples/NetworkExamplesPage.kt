package com.airbnb.lottie.sample.compose.examples

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieRetrySignal

@Composable
fun NetworkExamplesPage() {
    UsageExamplePageScaffold {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.height(16.dp))
            ExampleCard("Example 1", "Basic URL") {
                Example1()
            }
            ExampleCard("Example 2", "Fail with retries. Click to retry.") {
                Example2()
            }
        }
    }
}

@Composable
private fun Example1() {
    val composition by rememberLottieComposition(LottieCompositionSpec.Url("https://raw.githubusercontent.com/airbnb/lottie-android/master/sample/src/main/res/raw/heart.json"))
    LottieAnimation(composition)
}

@Composable
private fun Example2() {
    val retrySignal = rememberLottieRetrySignal()
    var failedCount by remember { mutableStateOf(0) }
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Url("not a url"),
        onRetry = { fc, _ ->
            failedCount = fc
            // Await the retry signal.
            retrySignal.awaitRetry()
            true
        }
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { retrySignal.retry() }
    ) {
        LottieAnimation(composition)
        Text(
            "Failed $failedCount times.\nAwaiting retry: ${retrySignal.isAwaitingRetry}",
            color = Color.LightGray,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomStart)
        )
    }
}