package com.airbnb.lottie.compose

import com.airbnb.lottie.LottieComposition

sealed class LottieCompositionResult {
    open val composition: LottieComposition? = null

    open operator fun invoke(): LottieComposition? = composition

    object Loading : LottieCompositionResult()

    data class Success(override val composition: LottieComposition) : LottieCompositionResult() {
        override operator fun invoke(): LottieComposition = composition
    }

    data class Fail(val e: Throwable) : LottieCompositionResult()
}