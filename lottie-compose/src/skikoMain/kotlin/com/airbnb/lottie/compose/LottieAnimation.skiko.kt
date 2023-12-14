package com.airbnb.lottie.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import org.jetbrains.skia.Rect
import org.jetbrains.skia.skottie.Animation
import org.jetbrains.skia.sksg.InvalidationController




@Composable
actual fun LottieAnimation(
    composition : LottieComposition?,
    progress : () -> Float,
    modifier: Modifier
) {

    val density = LocalDensity.current

    val defaultSize = remember(density, composition?.animation) {
        density.run {
            if (composition == null) {
                DpSize.Zero
            } else {
                DpSize(
                    width = composition.animation.width.toDp(),
                    height = composition.animation.height.toDp(),
                )
            }
        }
    }

    Canvas(
        modifier
            .size(defaultSize)
    ) {
        drawIntoCanvas {
            if (composition != null && !composition.animation.isClosed) {
                val currentProgress = progress()
                composition.animation.seek(currentProgress, composition.invalidationController)

                composition.animation.render(
                    canvas = it.nativeCanvas,
                    dst = Rect.makeWH(size.width, size.height)
                )
            }
        }
    }
}
