package com.airbnb.lottie.sample.compose.utils

import com.airbnb.lottie.LottieComposition

val LottieComposition.hasEmbeddedBitmaps get() = images?.any { (_, asset) -> asset.hasBitmap() } == true