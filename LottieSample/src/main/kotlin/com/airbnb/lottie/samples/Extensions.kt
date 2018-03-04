package com.airbnb.lottie.samples

import android.support.annotation.StringRes
import android.view.View

fun View.toggleActivated() {
    this.isActivated = !this.isActivated
}

fun View.getText(@StringRes res: Int) = this.resources.getText(res)

operator fun Boolean.inc() = !this