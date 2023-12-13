package com.airbnb.lottie.compose

import androidx.compose.runtime.Composable
import com.airbnb.lottie.LottieComposition

actual typealias LottieComposition = LottieComposition

internal actual val LottieComposition.fps: Float
    get() = frameRate

internal actual val LottieComposition.durationMillis: Float
    get() = duration


internal actual val LottieComposition.lastFrame: Float
    get() = endFrame

@Composable
actual fun rememberLottieComposition(spec: LottieCompositionSpec) : LottieCompositionResult = rememberLottieComposition(
    spec = spec,
    imageAssetsFolder = null
)

