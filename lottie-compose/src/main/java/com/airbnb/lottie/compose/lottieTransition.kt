package com.airbnb.lottie.compose

import androidx.compose.runtime.*
import com.airbnb.lottie.LottieComposition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect

@Composable
fun <T> lottieTransition(
    composition: LottieComposition?,
    state: T,
    animate: suspend (composition: LottieComposition?, progress: MutableState<Float>) -> Unit,
) {
    val progress = remember { mutableStateOf(0f) }
    val states = remember { MutableStateFlow(state) }
    states.value  = state

    LaunchedEffect(Unit) {
        states.collect {
            animate(composition, progress)
        }
    }
}