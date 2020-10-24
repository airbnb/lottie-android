package com.airbnb.lottie.sample.compose.api

import android.os.Parcelable
import com.airbnb.lottie.sample.compose.ui.toColorSafe
import kotlinx.android.parcel.Parcelize

data class AnimationDataV1(
    val id: Int,
    val bg_color: String?,
    val preview: String?,
    val title: String,
    val lottie_link: String
) {
    val bgColor get() = bg_color.toColorSafe()
}