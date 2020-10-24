package com.airbnb.lottie.compose

/**
 * Specification for a Lottie animation. Each subclass represents a different source.
 */
sealed class LottieAnimationSpec {
    data class RawRes(@androidx.annotation.RawRes val resId: Int) : LottieAnimationSpec()
    data class Url(val url: String) : LottieAnimationSpec()
    data class File(val fileName: String) : LottieAnimationSpec()
    data class Asset(val assetName: String) : LottieAnimationSpec()
}