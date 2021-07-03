package com.airbnb.lottie.samples.model

import android.os.Parcelable
import com.airbnb.lottie.samples.utils.toColorIntSafe
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimationDataV2(
        @SerializedName("bg_color") val bgColor: String,
        @SerializedName("file") val file: String,
        @SerializedName("id") val id: Int,
        @SerializedName("preview") val preview: String,
        @SerializedName("title") val title: String,
) : Parcelable {
    val bgColorInt get() = bgColor.toColorIntSafe()
}