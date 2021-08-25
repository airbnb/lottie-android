package com.airbnb.lottie.sample.compose.examples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCancellationBehavior
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.sample.compose.R
import kotlinx.coroutines.flow.collectLatest

enum class TransitionSection {
    Intro,
    LoopMiddle,
    Outro;

    fun next(): TransitionSection = when (this) {
        Intro -> LoopMiddle
        LoopMiddle -> Outro
        Outro -> Intro
    }
}

@Composable
fun TransitionsExamplesPage() {
    var state by remember { mutableStateOf(TransitionSection.Intro) }

    UsageExamplePageScaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
        ) {
            Text(
                "Single composition",
                modifier = Modifier
                    .padding(8.dp)
            )
            SingleCompositionTransition(state)
            Text(
                "Multiple compositions",
                modifier = Modifier
                    .padding(8.dp)
            )
            SplitCompositionTransition(state)
            TextButton(
                onClick = { state = state.next() }
            ) {
                Text("State: $state")
            }
        }

    }
}

@Composable
fun SingleCompositionTransition(section: TransitionSection) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bar))
    val animatable = rememberLottieAnimatable()
    val state by rememberUpdatedState(section)

    LaunchedEffect(composition, animatable) {
        composition ?: return@LaunchedEffect
        snapshotFlow { state }.collectLatest { s ->
            val clipSpec = when (s) {
                TransitionSection.Intro -> LottieClipSpec.Progress(0f, 0.301f)
                TransitionSection.LoopMiddle -> LottieClipSpec.Progress(0.301f, 2f / 3f)
                TransitionSection.Outro -> LottieClipSpec.Progress(2f / 3f, 1f)
            }
            do {
                animatable.animate(
                    composition,
                    clipSpec = clipSpec,
                    cancellationBehavior = LottieCancellationBehavior.OnIterationFinish,
                )
            } while (s == TransitionSection.LoopMiddle)
        }
    }
    LottieAnimation(composition, animatable.progress)
}

@Composable
fun SplitCompositionTransition(section: TransitionSection) {
    val introComposition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bar_1))
    val loopMiddleComposition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bar_2))
    val outroComposition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bar_3))
    val animatable = rememberLottieAnimatable()

    LaunchedEffect(section) {
        val composition = when (section) {
            TransitionSection.Intro -> introComposition
            TransitionSection.LoopMiddle -> loopMiddleComposition
            TransitionSection.Outro -> outroComposition
        }.await()
        animatable.animate(
            composition,
            iterations = if (section == TransitionSection.LoopMiddle) LottieConstants.IterateForever else 1,
            cancellationBehavior = LottieCancellationBehavior.OnIterationFinish,
        )
    }

    LottieAnimation(animatable.composition, animatable.progress)
}