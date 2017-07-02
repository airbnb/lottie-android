package com.airbnb.lottie.samples

import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment

fun Fragment.startActivity(cls: Class<*>) {
    startActivity(Intent(context, cls))
}

fun String.urlIntent(): Intent =
        Intent(Intent.ACTION_VIEW).setData(Uri.parse(this))