package com.airbnb.lottie.compose.renderer

import android.graphics.PointF
import androidx.annotation.FloatRange
import com.airbnb.lottie.model.content.ShapeData
import kotlin.math.roundToInt

fun lerp(a: Float, b: Float, @FloatRange(from = 0.0, to = 1.0) percentage: Float): Float = a + percentage * (b - a)

fun lerp(a: Int, b: Int, @FloatRange(from = 0.0, to = 1.0) percentage: Float): Int = (a + percentage * (b - a)).roundToInt()

inline fun <T, reified R> Collection<T>.firstInstanceOf(): R = first { it is R } as R