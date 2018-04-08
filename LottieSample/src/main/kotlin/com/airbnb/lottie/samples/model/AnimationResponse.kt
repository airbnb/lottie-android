package com.airbnb.lottie.samples.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

// This is a lint bug
@SuppressWarnings("ParcelCreator")
@Parcelize
data class AnimationResponse(
        val currentPage: Int,
        val data: List<AnimationData>,
        val from: String,
        val lastPage: Int,
        val nextPageUrl: String?,
        val path: String,
        val perPage: Int,
        val prevPageUrl: String,
        val to: Int,
        val total: Int
) : Parcelable