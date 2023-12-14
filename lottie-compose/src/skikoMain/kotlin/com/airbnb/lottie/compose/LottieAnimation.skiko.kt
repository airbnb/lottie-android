package com.airbnb.lottie.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toOffset
import org.jetbrains.skia.Rect
import org.jetbrains.skia.skottie.RenderFlag
import kotlin.math.roundToInt


@Composable
actual fun LottieAnimation(
    composition : LottieComposition?,
    progress : () -> Float,
    modifier: Modifier,
    alignment: Alignment,
    contentScale: ContentScale,
    clipToCompositionBounds : Boolean,
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

    val flags = remember(clipToCompositionBounds) {
        buildList {
            add(RenderFlag.SKIP_TOP_LEVEL_ISOLATION)
            if (!clipToCompositionBounds) {
                add(RenderFlag.DISABLE_TOP_LEVEL_CLIPPING)
            }
        }.toTypedArray()
    }

    val compositionSize = remember(composition?.animation) {
        composition?.animation?.size?.let { Size(it.x, it.y) } ?: Size.Zero
    }

    Canvas(modifier.size(defaultSize)) {
        drawIntoCanvas {
            if (composition == null || composition.animation.isClosed || composition.invalidationController.isClosed)
                return@drawIntoCanvas

            val scale = contentScale.computeScaleFactor(compositionSize, size)
            val intSize = size.round()
            val translation = alignment.align(compositionSize.round(), intSize, layoutDirection).toOffset()

            scale(scale.scaleX, scale.scaleY) {
                translate(translation.x, translation.y) {

                    composition.animation
                        .seek(progress(), composition.invalidationController)
                        .render(
                            canvas = it.nativeCanvas,
                            dst = Rect.makeWH(compositionSize.width, compositionSize.height),
                            *flags
                        )
                }
            }
        }
    }
}

private fun Size.round() = IntSize(width.roundToInt(), height.roundToInt())
