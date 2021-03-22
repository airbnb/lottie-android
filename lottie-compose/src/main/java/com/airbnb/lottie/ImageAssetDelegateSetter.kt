package com.airbnb.lottie

import com.airbnb.lottie.manager.ImageAssetManager

/**
 * Proxy for this internal API.
 */
internal fun LottieDrawable.setImageAssetManager(imageAssetManager: ImageAssetManager?) {
    @Suppress("RestrictedApi")
    this.setImageAssetManager(imageAssetManager)
}