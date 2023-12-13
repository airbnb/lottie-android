package com.airbnb.lottie.compose

import androidx.compose.runtime.Composable

/**
 * Holds animation data
 * */
expect class LottieComposition

internal expect val LottieComposition.fps : Float


internal expect val LottieComposition.durationMillis : Float


internal expect val LottieComposition.lastFrame : Float

/**
 * Create and remember lottie composition.
 *
 * @param data Lottie JSON string
 * */
@Composable
expect fun rememberLottieComposition(spec : LottieCompositionSpec) : LottieCompositionResult
