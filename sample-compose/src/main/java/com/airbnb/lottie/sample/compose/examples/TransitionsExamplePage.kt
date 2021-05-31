package com.airbnb.lottie.sample.compose.examples

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.airbnb.lottie.sample.compose.R
import kotlinx.coroutines.isActive

@Composable
fun TransitionsExamplePage() {
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    var state by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = { backPressedDispatcher?.onBackPressed() },
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
        ) {
            SingleCompositionTransition(state)
            Box(modifier = Modifier.height(16.dp))
            SplitCompositionTransition(state)
            TextButton(
                onClick = { state = (state + 1) % 3 }
            ) {
                val text = when (state) {
                    0 -> "Playing segment 1"
                    1 -> "Looping segment 2"
                    else -> "Playing segment 3"
                }
                Text(text)
            }
        }

    }
}

@Composable
fun SingleCompositionTransition(state: Int) {
    val compositionResult = lottieComposition(LottieCompositionSpec.RawRes(R.raw.bar))

    // This version of lottieTransition is for when your transition is segments of a single animation.
    // It just takes state and returns progress.
    val progress = lottieTransition(state) { progress ->
        compositionResult.awaitOrNull() ?: return@lottieTransition
        when (state) {
            0 -> {
                // This version of animateLottieComposition takes a MutableState<Float> as a parameter
                // and then suspends until one iteration through the animation is complete.
                animateLottieComposition(
                    compositionResult(),
                    progress,
                    clipSpec = LottieClipSpec.MinAndMaxProgress(0f, 0.301f),
                    cancellationBehavior = LottieCancellationBehavior.AtEnd,
                )
            }
            1 -> {
                // To loop a segment, just wrap this in a while loop.
                while (isActive) {
                    animateLottieComposition(
                        compositionResult(),
                        progress,
                        clipSpec = LottieClipSpec.MinAndMaxProgress(0.301f, 2f / 3f),
                        cancellationBehavior = LottieCancellationBehavior.AtEnd,
                    )
                }
            }
            2 -> {
                animateLottieComposition(
                    compositionResult(),
                    progress,
                    clipSpec = LottieClipSpec.MinAndMaxProgress(2f / 3f, 1f),
                    cancellationBehavior = LottieCancellationBehavior.AtEnd,
                )
            }
        }
    }
    LottieAnimation(compositionResult(), progress)
}

@Composable
fun SplitCompositionTransition(state: Int) {
    val compositionResult1 = lottieComposition(LottieCompositionSpec.RawRes(R.raw.bar_1))
    val compositionResult2 = lottieComposition(LottieCompositionSpec.RawRes(R.raw.bar_2))
    val compositionResult3 = lottieComposition(LottieCompositionSpec.RawRes(R.raw.bar_3))

    // This version of lottieTransition is for when your transition uses different composition for different segments.
    // It takes a second lambda to return the correct composition for the given state and then returns both the compositiojn
    // and the progress. Make sure to use the returned composition, not the one corresponding to state.
    // Right after a state changes, if the previous transition was using LottieCancellationBehavior.AtEnd, it may
    // continue to animate for a short period _after_ the state changes.
    val (compositionResult, progress) = lottieTransition(
        state,
        compositionForState = {
            when (state) {
                0 -> compositionResult1
                1 -> compositionResult2
                else -> compositionResult3
            }
        },
    ) { compositionResult, progress ->
        compositionResult.awaitOrNull() ?: return@lottieTransition
        when (state) {
            0 -> {
                animateLottieComposition(
                    compositionResult(),
                    progress,
                    cancellationBehavior = LottieCancellationBehavior.AtEnd,
                )
            }
            1 -> {
                while (isActive) {
                    animateLottieComposition(
                        compositionResult(),
                        progress,
                        cancellationBehavior = LottieCancellationBehavior.AtEnd,
                    )
                }
            }
            else -> {
                animateLottieComposition(
                    compositionResult(),
                    progress,
                    cancellationBehavior = LottieCancellationBehavior.AtEnd,
                )
            }
        }
    }
    LottieAnimation(compositionResult(), progress)
}