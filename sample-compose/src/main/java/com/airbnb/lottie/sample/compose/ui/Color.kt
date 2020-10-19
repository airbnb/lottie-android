package com.airbnb.lottie.sample.compose.ui

import android.util.Log
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.airbnb.lottie.L

val purple200 = Color(0xFFBB86FC)
val purple500 = Color(0xFF6200EE)
val purple700 = Color(0xFF3700B3)
val teal200 = Color(0xFF03DAC5)
val textColorDark = Color(0xFF484848)

val bottomBarBackground = Color(0xFFF7F7F7)

@ColorInt
fun String?.toColorSafe(): Color {
    var bgColor = this ?: "#ffffff"
    bgColor = if (bgColor.startsWith("#")) bgColor else "#$bgColor"

    val colorInt = try {
        when (bgColor.length) {
            0 -> "#ffffff"
            4 -> "#%c%c%c%c%c%c".format(
                bgColor[1], bgColor[1],
                bgColor[2], bgColor[2],
                bgColor[3], bgColor[3]
            )
            5 -> "#%c%c%c%c%c%c%c%c".format(
                bgColor[1], bgColor[1],
                bgColor[2], bgColor[2],
                bgColor[3], bgColor[3],
                bgColor[4], bgColor[4]
            )
            else -> bgColor
        }.toColorInt()
    } catch (e: IllegalArgumentException) {
        Log.w(L.TAG, "Unable to parse $bgColor.")
        android.graphics.Color.WHITE
    }
    return Color(colorInt)
}
