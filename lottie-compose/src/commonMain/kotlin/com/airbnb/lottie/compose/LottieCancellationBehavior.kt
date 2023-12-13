package com.airbnb.lottie.compose

/**
 * Determines how the animation should behave if the calling CoroutineScope is cancelled.
 *
 * @see rememberLottieAnimatable
 */
enum class LottieCancellationBehavior {
    /**
     * Stop animation immediately and return early.
     */
    Immediately,

    /**
     * Delay cancellations until the current iteration has fully completed.
     * This can be useful in state based transitions where you want one animation to finish its
     * animation before continuing to the next.
     */
    OnIterationFinish,
}