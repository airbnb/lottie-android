package com.airbnb.lottie.samples.model

import android.graphics.Color
import android.os.Parcelable
import android.support.annotation.ColorInt
import android.text.TextUtils
import androidx.graphics.toColorInt
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AnimationData(
        val id: Long,
        val title: String,
        val description: String?,
        private val bgColor: String?,
        val aepFile: String,
        val bodymovinVersion: String,
        val slug: String,
        val speed: String,
        val preview: String,
        val lottieLink: String,
        val userInfo: UserInfo?
) : Parcelable {
    @ColorInt
    fun bgColorInt(): Int {
        val bgColor = this.bgColor ?: ""
        return when(bgColor.length) {
            0 -> "#ffffff"
            4 -> "#%c%c%c%c%c%c".format(
                    bgColor[1], bgColor[1],
                    bgColor[2], bgColor[2],
                    bgColor[3], bgColor[3]
            )
            else -> bgColor
        } .toColorInt()
    }
}