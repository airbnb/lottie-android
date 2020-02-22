package com.airbnb.lottie.samples.model

import android.os.Parcelable
import com.airbnb.lottie.samples.toColorIntSafe
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AnimationDataV2(
        val aepFlag: Int,
        val baseprice: String,
        val bgColor: String,
        val bodymovinVersion: String?,
        val createdAt: String,
        val dataFile: String,
        val description: String,
        val downloads: Int,
        val file: String,
        val fileHash: String,
        val id: Int,
        val inProcess: Int,
        val isSticker: Int,
        val preview: String,
        val previewFrame: Int,
        val previewUrl: String,
        val previewVideo: String,
        val previewVideoUrl: String,
        val price: Int,
        val publishedAt: String,
        val slug: String,
        val speed: Int,
        val status: Int,
        val title: String,
        val updated_at: String,
        val userId: Int,
        val videoFramerate: Int,
        val views: Int
) : Parcelable {
    val bgColorInt get() = bgColor.toColorIntSafe()

}