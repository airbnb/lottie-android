package com.airbnb.lottie.sample.compose.api

import android.os.Parcelable
import com.airbnb.lottie.sample.compose.ui.toColorSafe
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AnimationData(
    val id: Int,
    @Json(name = "bgColor") private val bgColorString: String?,
    val preview_url: String?,
    val title: String,
    val file: String
) : Parcelable {
    val bgColor get() = bgColorString.toColorSafe()
}