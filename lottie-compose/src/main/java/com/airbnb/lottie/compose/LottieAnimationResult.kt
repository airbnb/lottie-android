package com.airbnb.lottie.compose

sealed class LottieAnimationResult {
    abstract val lastFrameTime: Long?

    class Cancelled(override val lastFrameTime: Long?) : LottieAnimationResult()

    class Finished(override val lastFrameTime: Long) : LottieAnimationResult()
}