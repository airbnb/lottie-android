package com.airbnb.lottie.samples.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

// This is a lint bug
@SuppressWarnings("ParcelCreator")
@Parcelize
data class UserInfo(
        val id: Long,
        val name: String,
        val bio: String?,
        val location: String?,
        val city: String?,
        val social_twitter: String?,
        val social_dribbble: String?,
        val social_behance: String?,
        val url: String?
) : Parcelable