package com.airbnb.lottie.sample.compose.api

data class AnimationsResponseV1(
    val data: List<AnimationDataV1>,
    val current_page: Int,
    val last_page: Int
)