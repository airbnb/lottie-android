package com.airbnb.lottie.compose

/**
 * Determines how the animation should behave if the calling CoroutineScope is cancelled.
 */
enum class LottieCancellationBehavior {
    /**
     * Stop animation immediately and return early.
     *
     * @see lottieTransition
     */
    Immediately,

    OnFinish,
}