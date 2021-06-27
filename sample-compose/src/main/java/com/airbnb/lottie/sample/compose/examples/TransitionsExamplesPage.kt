package com.airbnb.lottie.sample.compose.examples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
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
import com.airbnb.lottie.compose.LottieAnimatable
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCancellationBehavior
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
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
            SingleCompositionTransition(state)
            Box(modifier = Modifier.height(16.dp))
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
    val animatable = remember { LottieAnimatable() }
    val state by rememberUpdatedState(section)

    LaunchedEffect(composition, animatable) {
        composition ?: return@LaunchedEffect
        snapshotFlow { state }.collectLatest { s ->
            val clipSpec = when (s) {
                TransitionSection.Intro -> LottieClipSpec.MinAndMaxProgress(0f, 0.301f)
                TransitionSection.LoopMiddle -> LottieClipSpec.MinAndMaxProgress(0.301f, 2f / 3f)
                TransitionSection.Outro -> LottieClipSpec.MinAndMaxProgress(2f / 3f, 1f)
            }
            do {
                animatable.animate(
                    composition,
                    clipSpec = clipSpec,
                    cancellationBehavior = LottieCancellationBehavior.OnFinish,
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
    val animatable = remember { LottieAnimatable() }
    val state by rememberUpdatedState(section)

    LaunchedEffect(animatable) {
        snapshotFlow { state }.collectLatest { s ->
            val composition = when (s) {
                TransitionSection.Intro -> introComposition
                TransitionSection.LoopMiddle -> loopMiddleComposition
                TransitionSection.Outro -> outroComposition
            }.await()
            do {
                animatable.animate(
                    composition,
                    initialProgress = 0f,
                    cancellationBehavior = LottieCancellationBehavior.OnFinish,
                )
            } while (s == TransitionSection.LoopMiddle)
        }
    }


    LottieAnimation(animatable.composition, animatable.progress)
}