package com.airbnb.lottie.compose

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun <T> lottieTransition(
    state: T,
    animate: suspend CoroutineScope.(progress: MutableState<Float>) -> Unit,
): State<Float> {
    val progress = remember { mutableStateOf(0f) }
    val states = remember { MutableStateFlow(state) }
    states.value  = state

    LaunchedEffect(Unit) {
        states.collectLatest {
            coroutineScope {
                animate(progress)
            }
        }
    }
    return progress
}