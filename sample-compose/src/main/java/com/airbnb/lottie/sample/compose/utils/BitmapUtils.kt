package com.airbnb.lottie.sample.compose.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.airbnb.lottie.LottieImageAsset

fun LottieImageAsset.toDummyBitmap(strokeWidth: Float): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()
    paint.color = Color.GRAY
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = strokeWidth
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    canvas.drawLine(0f, 0f, width.toFloat(), height.toFloat(), paint)
    canvas.drawLine(width.toFloat(), 0f, 0f, height.toFloat(), paint)
    return bitmap
}