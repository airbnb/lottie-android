package com.airbnb.lottie.compose


/**
 * Specification for a [com.airbnb.lottie.LottieComposition]. Each subclass represents a different source.
 * A [com.airbnb.lottie.LottieComposition] is the stateless parsed version of a Lottie json file and is
 * passed into [rememberLottieComposition] or [LottieAnimation].
 */
expect sealed interface LottieCompositionSpec {
    companion object
}

/**
 * Load an animation from its json string.
 */
expect fun LottieCompositionSpec.Companion.JsonString(jsonString: String) : LottieCompositionSpec
