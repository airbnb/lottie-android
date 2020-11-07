package com.airbnb.lottie.samples.model

import android.os.Parcelable
import com.airbnb.lottie.samples.utils.toColorIntSafe
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AnimationData(
        val id: Long,
        val title: String,
        val description: String?,
        private val bgColor: String?,
        val preview: String?,
        val lottieLink: String,
        val userInfo: UserInfo?
) : Parcelable {
    val bgColorInt get() = bgColor.toColorIntSafe()
}