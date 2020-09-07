package com.airbnb.lottie.sample.compose.api

import android.os.Parcelable
import com.airbnb.lottie.sample.compose.ui.toColorSafe
import com.squareup.moshi.Json

data class AnimationDataV2(
    val id: Int,
    @Json(name = "bgColor") private val bgColorString: String,
    val preview_url: String?,
    val title: String,
) {
    val bgColor get() = bgColorString.toColorSafe()

}