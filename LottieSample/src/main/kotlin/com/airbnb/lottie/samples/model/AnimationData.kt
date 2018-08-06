package com.airbnb.lottie.samples.model

import android.graphics.Color
import android.os.Parcelable
import android.support.annotation.ColorInt
import android.util.Log
import androidx.graphics.toColorInt
import com.airbnb.lottie.L
import kotlinx.android.parcel.Parcelize

// This is a lint bug
@SuppressWarnings("ParcelCreator")
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
        val preview: String?,
        val lottieLink: String,
        val userInfo: UserInfo?
) : Parcelable {
    @ColorInt
    fun bgColorInt(): Int {
        var bgColor = this.bgColor ?: "#ffffff"
        bgColor = if (bgColor.startsWith("#")) bgColor else "#$bgColor"

        return try {
            when (bgColor.length) {
                0 -> "#ffffff"
                4 -> "#%c%c%c%c%c%c".format(
                        bgColor[1], bgColor[1],
                        bgColor[2], bgColor[2],
                        bgColor[3], bgColor[3]
                )
                5 -> "#%c%c%c%c%c%c%c%c".format(
                        bgColor[1], bgColor[1],
                        bgColor[2], bgColor[2],
                        bgColor[3], bgColor[3],
                        bgColor[4], bgColor[4]
                )
                else -> bgColor
            }.toColorInt()
        } catch (e: IllegalArgumentException) {
            Log.w(L.TAG, "Unable to parse $bgColor.")
            Color.WHITE
        }
    }
}