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

    /**
     * Delay cancellations until the animation has fully completed.
     * This can be useful in state based transitions where you want one animation to finish its
     * animation before continuing to the next.
     *
     *
     * This CANNOT be used with [LottieConstants.IterateForever] because that would prevent the animation
     * from ever completing.
     */
    OnFinish,
}