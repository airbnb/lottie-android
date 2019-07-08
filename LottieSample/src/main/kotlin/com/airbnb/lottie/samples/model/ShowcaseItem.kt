package com.airbnb.lottie.samples.model

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes

data class ShowcaseItem(
        @DrawableRes val drawableRes: Int,
        @StringRes val titleRes: Int,
        val clickListener: () -> Unit
)