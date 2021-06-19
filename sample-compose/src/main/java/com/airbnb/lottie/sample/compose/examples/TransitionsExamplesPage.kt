package com.airbnb.lottie.sample.compose.examples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.airbnb.lottie.sample.compose.R
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

enum class TransitionState {
    Intro,
    LoopMiddle,
    Outro;

    fun next(): TransitionState = when (this) {
        Intro -> LoopMiddle
        LoopMiddle -> Outro
        Outro -> Intro
    }
}

@Composable
fun TransitionsExamplesPage() {
    val state = remember { mutableStateOf(TransitionState.Intro) }

    UsageExamplePageScaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
        ) {
            SingleCompositionTransition(state)
            Box(modifier = Modifier.height(16.dp))
            SplitCompositionTransition(state)
            TextButton(
                onClick = { state.value = state.value.next() }
            ) {
                Text("State: ${state.value}")
            }
        }

    }
}

@Composable
fun SingleCompositionTransition(state: State<TransitionState>) {
    val composition by lottieComposition(LottieCompositionSpec.RawRes(R.raw.bar))
    val animatable = remember { LottieAnimatable() }

    LaunchedEffect(composition, animatable) {
        composition ?: return@LaunchedEffect
        snapshotFlow { state.value }.collectLatest { s ->
            val clipSpec = when (s) {
                TransitionState.Intro -> LottieClipSpec.MinAndMaxProgress(0f, 0.301f)
                TransitionState.LoopMiddle -> LottieClipSpec.MinAndMaxProgress(0.301f, 2f / 3f)
                TransitionState.Outro -> LottieClipSpec.MinAndMaxProgress(2f / 3f, 1f)
            }
            do {
                animatable.animate(
                    composition,
                    clipSpec = clipSpec,
                    cancellationBehavior = LottieCancellationBehavior.OnFinish,
                )
            } while (s == TransitionState.LoopMiddle)
        }
    }
    LottieAnimation(composition, animatable.progress)
}

@Composable
fun SplitCompositionTransition(state: State<TransitionState>) {
    val introComposition = lottieComposition(LottieCompositionSpec.RawRes(R.raw.bar_1))
    val loopMiddleComposition = lottieComposition(LottieCompositionSpec.RawRes(R.raw.bar_2))
    val outroComposition = lottieComposition(LottieCompositionSpec.RawRes(R.raw.bar_3))
    val animatable = remember { LottieAnimatable() }

    LaunchedEffect(animatable) {
        snapshotFlow { state.value }.collectLatest { s ->
            val composition = when (state.value) {
                TransitionState.Intro -> introComposition
                TransitionState.LoopMiddle -> loopMiddleComposition
                TransitionState.Outro -> outroComposition
            }.await()
            do {
                animatable.animate(
                    composition,
                    initialProgress = 0f,
                    cancellationBehavior = LottieCancellationBehavior.OnFinish,
                )
            } while (s == TransitionState.LoopMiddle)
        }
    }


    LottieAnimation(animatable.composition, animatable.progress)
}