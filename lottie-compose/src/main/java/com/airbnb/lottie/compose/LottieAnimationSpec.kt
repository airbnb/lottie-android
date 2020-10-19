package com.airbnb.lottie.compose

/**
 * Specification for a Lottie animation. Each subclass represents a different source.
 */
sealed class LottieAnimationSpec {
    class RawRes(@androidx.annotation.RawRes val resId: Int) : LottieAnimationSpec()
    class Url(val url: String) : LottieAnimationSpec()
    class File(val fileName: String) : LottieAnimationSpec()
    class Asset(val assetName: String) : LottieAnimationSpec()
}