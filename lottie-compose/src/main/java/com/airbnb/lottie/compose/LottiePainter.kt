package com.airbnb.lottie.compose

import android.graphics.Matrix
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.utils.Utils
import kotlin.math.roundToInt

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
    painter.composition.value = composition
    painter.progress.floatValue = progress
    painter.outlineMasksAndMattes.value = outlineMasksAndMattes
    painter.applyOpacityToLayers.value = applyOpacityToLayers
    painter.enableMergePaths.value = enableMergePaths
    painter.renderMode.value = renderMode
    painter.maintainOriginalImageBounds.value = maintainOriginalImageBounds
    painter.dynamicProperties.value = dynamicProperties
    painter.clipToCompositionBounds.value = clipToCompositionBounds
    painter.clipTextToBoundingBox.value = clipTextToBoundingBox
    painter.fontMap.value = fontMap
    painter.asyncUpdates.value = asyncUpdates
    return painter
}

class LottiePainter(
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
    val composition = mutableStateOf(composition)
    val progress = mutableFloatStateOf(progress)
    val outlineMasksAndMattes = mutableStateOf(outlineMasksAndMattes)
    val applyOpacityToLayers = mutableStateOf(applyOpacityToLayers)
    val enableMergePaths = mutableStateOf(enableMergePaths)
    val renderMode = mutableStateOf(renderMode)
    val maintainOriginalImageBounds = mutableStateOf(maintainOriginalImageBounds)
    val dynamicProperties = mutableStateOf(dynamicProperties)
    private var setDynamicProperties: LottieDynamicProperties? = null
    val clipToCompositionBounds = mutableStateOf(clipToCompositionBounds)
    val fontMap = mutableStateOf(fontMap)
    val asyncUpdates = mutableStateOf(asyncUpdates)
    val clipTextToBoundingBox = mutableStateOf(clipTextToBoundingBox)

    private val drawable = LottieDrawable()
    private val matrix = Matrix()
    override val intrinsicSize: Size
        get() {
            val composition = composition.value ?: return Size.Unspecified
            return Size(composition.bounds.width().toFloat(), composition.bounds.height().toFloat())
        }

    override fun DrawScope.onDraw() {
        val composition = composition.value ?: return
        drawIntoCanvas { canvas ->
            val compositionSize = Size(composition.bounds.width().toFloat(), composition.bounds.height().toFloat())
            val intSize = IntSize(size.width.roundToInt(), size.height.roundToInt())

            matrix.reset()
            matrix.preScale(intSize.width / compositionSize.width, intSize.height / compositionSize.height)

            drawable.enableMergePathsForKitKatAndAbove(enableMergePaths.value)
            drawable.renderMode = renderMode.value
            drawable.asyncUpdates = asyncUpdates.value
            drawable.composition = composition
            drawable.setFontMap(fontMap.value)
            if (dynamicProperties.value !== setDynamicProperties) {
                setDynamicProperties?.removeFrom(drawable)
                dynamicProperties.value?.addTo(drawable)
                setDynamicProperties = dynamicProperties.value
            }
            drawable.setOutlineMasksAndMattes(outlineMasksAndMattes.value)
            drawable.isApplyingOpacityToLayersEnabled = applyOpacityToLayers.value
            drawable.maintainOriginalImageBounds = maintainOriginalImageBounds.value
            drawable.clipToCompositionBounds = clipToCompositionBounds.value
            drawable.clipTextToBoundingBox = clipTextToBoundingBox.value
            drawable.progress = progress.floatValue
            drawable.setBounds(0, 0, composition.bounds.width(), composition.bounds.height())
            drawable.draw(canvas.nativeCanvas, matrix)
        }

    }
}

private operator fun Size.times(scale: ScaleFactor): IntSize {
    return IntSize((width * scale.scaleX).toInt(), (height * scale.scaleY).toInt())
}
