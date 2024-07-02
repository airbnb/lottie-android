package com.airbnb.lottie.compose

import android.graphics.Matrix
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.unit.IntSize
import com.airbnb.lottie.AsyncUpdates
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieFeatureFlag
import com.airbnb.lottie.RenderMode
import kotlin.math.roundToInt

/**
 * A composable that makes it easy to create a [LottiePainter] and update its properties.
 */
@Composable
fun rememberLottiePainter(
    composition: LottieComposition? = null,
    progress: Float = 0f,
    outlineMasksAndMattes: Boolean = false,
    applyOpacityToLayers: Boolean = false,
    enableMergePaths: Boolean = false,
    renderMode: RenderMode = RenderMode.AUTOMATIC,
    maintainOriginalImageBounds: Boolean = false,
    dynamicProperties: LottieDynamicProperties? = null,
    clipToCompositionBounds: Boolean = true,
    clipTextToBoundingBox: Boolean = false,
    fontMap: Map<String, Typeface>? = null,
    asyncUpdates: AsyncUpdates = AsyncUpdates.AUTOMATIC,
): LottiePainter {
    val painter = remember { LottiePainter() }
    painter.composition = composition
    painter.progress = progress
    painter.outlineMasksAndMattes = outlineMasksAndMattes
    painter.applyOpacityToLayers = applyOpacityToLayers
    painter.enableMergePaths = enableMergePaths
    painter.renderMode = renderMode
    painter.maintainOriginalImageBounds = maintainOriginalImageBounds
    painter.dynamicProperties = dynamicProperties
    painter.clipToCompositionBounds = clipToCompositionBounds
    painter.clipTextToBoundingBox = clipTextToBoundingBox
    painter.fontMap = fontMap
    painter.asyncUpdates = asyncUpdates
    return painter
}

/**
 * A [Painter] that renders a [LottieComposition].
 */
class LottiePainter internal constructor(
    composition: LottieComposition? = null,
    progress: Float = 0f,
    outlineMasksAndMattes: Boolean = false,
    applyOpacityToLayers: Boolean = false,
    enableMergePaths: Boolean = false,
    renderMode: RenderMode = RenderMode.AUTOMATIC,
    maintainOriginalImageBounds: Boolean = false,
    dynamicProperties: LottieDynamicProperties? = null,
    clipToCompositionBounds: Boolean = true,
    clipTextToBoundingBox: Boolean = false,
    fontMap: Map<String, Typeface>? = null,
    asyncUpdates: AsyncUpdates = AsyncUpdates.AUTOMATIC,
) : Painter() {
    internal var composition by mutableStateOf(composition)
    internal var progress by mutableFloatStateOf(progress)
    internal var outlineMasksAndMattes by mutableStateOf(outlineMasksAndMattes)
    internal var applyOpacityToLayers by mutableStateOf(applyOpacityToLayers)
    internal var enableMergePaths by mutableStateOf(enableMergePaths)
    internal var renderMode by mutableStateOf(renderMode)
    internal var maintainOriginalImageBounds by mutableStateOf(maintainOriginalImageBounds)
    internal var dynamicProperties by mutableStateOf(dynamicProperties)
    internal var clipToCompositionBounds by mutableStateOf(clipToCompositionBounds)
    internal var fontMap by mutableStateOf(fontMap)
    internal var asyncUpdates by mutableStateOf(asyncUpdates)
    internal var clipTextToBoundingBox by mutableStateOf(clipTextToBoundingBox)

    private var setDynamicProperties: LottieDynamicProperties? = null

    private val drawable = LottieDrawable()
    private val matrix = Matrix()
    override val intrinsicSize: Size
        get() {
            val composition = composition ?: return Size.Unspecified
            return Size(composition.bounds.width().toFloat(), composition.bounds.height().toFloat())
        }

    override fun DrawScope.onDraw() {
        val composition = composition ?: return
        drawIntoCanvas { canvas ->
            val compositionSize = Size(composition.bounds.width().toFloat(), composition.bounds.height().toFloat())
            val intSize = IntSize(size.width.roundToInt(), size.height.roundToInt())

            matrix.reset()
            matrix.preScale(intSize.width / compositionSize.width, intSize.height / compositionSize.height)

            drawable.enableFeatureFlag(LottieFeatureFlag.MergePathsApi19, enableMergePaths)
            drawable.renderMode = renderMode
            drawable.asyncUpdates = asyncUpdates
            drawable.composition = composition
            drawable.setFontMap(fontMap)
            if (dynamicProperties !== setDynamicProperties) {
                setDynamicProperties?.removeFrom(drawable)
                dynamicProperties?.addTo(drawable)
                setDynamicProperties = dynamicProperties
            }
            drawable.setOutlineMasksAndMattes(outlineMasksAndMattes)
            drawable.isApplyingOpacityToLayersEnabled = applyOpacityToLayers
            drawable.maintainOriginalImageBounds = maintainOriginalImageBounds
            drawable.clipToCompositionBounds = clipToCompositionBounds
            drawable.clipTextToBoundingBox = clipTextToBoundingBox
            drawable.progress = progress
            drawable.setBounds(0, 0, composition.bounds.width(), composition.bounds.height())
            drawable.draw(canvas.nativeCanvas, matrix)
        }

    }
}

private operator fun Size.times(scale: ScaleFactor): IntSize {
    return IntSize((width * scale.scaleX).toInt(), (height * scale.scaleY).toInt())
}
