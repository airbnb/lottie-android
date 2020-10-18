package com.airbnb.lottie.compose

import com.airbnb.lottie.LottieComposition

/**
 * Specification for a Lottie animation. Each subclass represents a different source.
 */
sealed class LottieAnimationSpec {
    class RawRes(@androidx.annotation.RawRes val resId: Int) : LottieAnimationSpec()
    class Url(val url: String) : LottieAnimationSpec()
}