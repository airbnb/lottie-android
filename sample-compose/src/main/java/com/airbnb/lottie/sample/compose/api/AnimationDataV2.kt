package com.airbnb.lottie.sample.compose.api

import com.airbnb.lottie.sample.compose.ui.toColorSafe

data class AnimationDataV2(
    val id: Int,
    val bg_color: String?,
    val preview_url: String?,
    val title: String,
    val file: String
) {

    constructor(data: AnimationDataV1) : this(
        data.id,
        data.bg_color,
        data.preview,
        data.title,
        data.lottie_link,
    )

    val bgColor get() = bg_color.toColorSafe()
}