package com.airbnb.lottie.samples

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.annotation.DrawableRes
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

fun Fragment.startActivity(cls: Class<*>) {
    startActivity(Intent(context, cls))
}

fun String.urlIntent(): Intent =
        Intent(Intent.ACTION_VIEW).setData(Uri.parse(this))

fun ViewGroup.inflate(@LayoutRes layout: Int, attachToRoot: Boolean = true): View =
        LayoutInflater.from(context).inflate(layout, this, attachToRoot)

fun String.hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, this) == PackageManager.PERMISSION_GRANTED

fun TextView.setDrawableLeft(@DrawableRes drawableRes: Int, activity: Activity) {
    val drawable = VectorDrawableCompat.create(resources, drawableRes, activity.theme)
    setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
}

fun View.showSnackbarLong(@StringRes message: Int) =
        showSnackbarLong(resources.getString(message))

fun View.showSnackbarLong(message: String) =
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()

fun View.setVisibleIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

fun ImageView.setImageUrl(url: String?) = url?.let { Glide.with(this).load(it).into(this) }

inline fun <reified T> flatten(vararg lists: List<T>?) = lists.flatMap { it ?: emptyList() }

fun Float.lerp(other: Float, amount: Float): Float = this + amount * (other - this)

fun Float.sqrt() = Math.sqrt(this.toDouble()).toFloat()

fun View.getText(@StringRes res: Int) = this.resources.getText(res)
operator fun Boolean.inc() = !this

fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Vibrator.vibrateCompat(millis: Long) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrate(millis)
    }
}