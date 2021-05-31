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
                Text("State is $state")
            }
        }

    }
}

@Composable
fun SingleCompositionTransition(state: Int) {
    val compositionResult = lottieComposition(LottieCompositionSpec.RawRes(R.raw.bar))

    val progress by lottieTransition(state) { progress ->
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
                        clipSpec = LottieClipSpec.MinAndMaxProgress(0.301f, 0.66f),
                        cancellationBehavior = LottieCancellationBehavior.AtEnd,
                    )
                }
            }
            2 -> {
                animateLottieComposition(
                    compositionResult(),
                    progress,
                    clipSpec = LottieClipSpec.MinAndMaxProgress(0.66f, 1f),
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

    var compositionResult by remember { mutableStateOf(compositionResult1) }

    val progress by lottieTransition(state) { progress ->
        compositionResult = when (state) {
            0 -> compositionResult1
            1 -> compositionResult2
            else -> compositionResult3
        }
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
            2 -> {
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