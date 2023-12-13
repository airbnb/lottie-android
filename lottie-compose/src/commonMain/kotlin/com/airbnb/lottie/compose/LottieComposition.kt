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
 * Takes a [LottieCompositionSpec], attempts to load and parse the animation, and returns a [LottieCompositionResult].
 *
 * [LottieCompositionResult] allows you to explicitly check for loading, failures, call
 * [LottieCompositionResult.await], or invoke it like a function to get the nullable composition.
 *
 * [LottieCompositionResult] implements State<LottieComposition?> so if you don't need the full result class,
 * you can use this function like:
 * ```
 * val compositionResult: LottieCompositionResult = lottieComposition(spec)
 * // or...
 * val composition: State<LottieComposition?> by lottieComposition(spec)
 * ```
 *
 * The loaded composition will automatically load and set images that are embedded in the json as a base64 string
 * or will load them from assets if an imageAssetsFolder is supplied.
 *
 * @param spec The [LottieCompositionSpec] that defines which LottieComposition should be loaded.
 */
@Composable
expect fun rememberLottieComposition(spec : LottieCompositionSpec) : LottieCompositionResult
