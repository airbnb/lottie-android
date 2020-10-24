package com.airbnb.lottie.sample.compose.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val ColorPalette = lightColors(
        primary = Teal,
        primaryVariant = TealDark,
        secondary = purple200
)

@Composable
fun LottieTheme(content: @Composable () -> Unit) {
    MaterialTheme(
            colors = ColorPalette,
            typography = typography,
            shapes = shapes,
            content = content
    )
}