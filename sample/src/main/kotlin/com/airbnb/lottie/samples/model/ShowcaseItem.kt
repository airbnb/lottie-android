package com.airbnb.lottie.samples.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class ShowcaseItem(
        @DrawableRes val drawableRes: Int,
        @StringRes val titleRes: Int,
        val clickListener: () -> Unit
)