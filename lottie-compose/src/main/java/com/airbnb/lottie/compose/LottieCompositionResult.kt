package com.airbnb.lottie.compose

import com.airbnb.lottie.LottieComposition

/**
 * A [LottieCompositionResult] subclass is returned from [rememberLottieCompositionResult].
 *
 * To access a [LottieComposition] from a [LottieCompositionResult], call [invoke] directly or as an operator.
 */
sealed class LottieCompositionResult {
    open val composition: LottieComposition? = null

    /**
     * This is an operator so an instance of [LottieCompositionResult] can be called like a function
     * instead of calling [invoke] by name.
     *
     * @return the composition if successful or null of it is is still loading or failed to load.
     */
    open operator fun invoke(): LottieComposition? = composition

    object Loading : LottieCompositionResult()

    data class Success(override val composition: LottieComposition) : LottieCompositionResult() {
        override operator fun invoke(): LottieComposition = composition
    }

    data class Fail(val e: Throwable) : LottieCompositionResult()
}