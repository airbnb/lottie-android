package com.airbnb.lottie.sample.compose.api

data class AnimationsResponseV2(
    val data: List<AnimationDataV2>,
    val current_page: Int,
    val last_page: Int
)