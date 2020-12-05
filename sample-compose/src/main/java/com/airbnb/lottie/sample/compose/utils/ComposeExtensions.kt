package com.airbnb.lottie.sample.compose.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.maybeBackground(color: Color?): Modifier {
    return if (color == null) {
        this
    } else {
        this.then(background(color))
    }
}


fun Modifier.drawTopBorder(color: Color = Color.DarkGray) = this.then(drawBehind {
    drawRect(color, Offset.Zero, size = Size(size.width, 1f))
})


fun Modifier.maybeDrawBorder(draw: Boolean, color: Color = Color.Black, width: Dp = 1.dp): Modifier {
    return if (draw) {
        this.then(border(width, color))
    } else {
        this
    }
}
