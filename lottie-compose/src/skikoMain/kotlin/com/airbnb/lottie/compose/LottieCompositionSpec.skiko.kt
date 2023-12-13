package com.airbnb.lottie.compose

import kotlin.jvm.JvmInline

actual sealed interface LottieCompositionSpec {
    /**
     * Load an animation from its json string.
     */
    @JvmInline
    value class JsonString(val jsonString: String) : LottieCompositionSpec

    actual companion object
}


actual fun LottieCompositionSpec.Companion.JsonString(jsonString: String) : LottieCompositionSpec =
    LottieCompositionSpec.JsonString(jsonString)
