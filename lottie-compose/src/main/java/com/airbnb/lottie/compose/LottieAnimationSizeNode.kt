package com.airbnb.lottie.compose

import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.constrain

@Stable
internal fun Modifier.lottieSize(width: Int, height: Int, contentScale: ContentScale) = this.then(LottieAnimationSizeElement(width, height, contentScale))

internal data class LottieAnimationSizeElement(
    val width: Int,
    val height: Int,
    val contentScale: ContentScale,
) : ModifierNodeElement<LottieAnimationSizeNode>() {
    override fun create(): LottieAnimationSizeNode {
        return LottieAnimationSizeNode(width, height, contentScale)
    }

    override fun update(node: LottieAnimationSizeNode) {
        node.width = width
        node.height = height
        node.contentScale = contentScale
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "Lottie Size"
        properties["width"] = width
        properties["height"] = height
        properties["contentScale"] = contentScale
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LottieAnimationSizeElement) return false

        if (width != other.width) return false
        if (height != other.height) return false
        if (contentScale != other.contentScale) return false
        return true
    }

    override fun hashCode(): Int {
        var result = width.hashCode()
        result = 31 * result + height.hashCode()
        return result
    }
}


internal class LottieAnimationSizeNode(
    var width: Int,
    var height: Int,
    var contentScale: ContentScale,
) : Modifier.Node(), LayoutModifierNode {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val constrainedSize = constraints.constrain(IntSize(width, height))
        val wrappedConstraints = when {
            // There is enough space for the width and height so we don't need to constrain.
            constrainedSize.width >= width && constrainedSize.height >= height -> Constraints(
                minWidth = width,
                maxWidth = width,
                minHeight = height,
                maxHeight = height,
            )
            else -> {
                val compositionSize = Size(width.toFloat(), height.toFloat())
                val scale = contentScale.computeScaleFactor(compositionSize, Size(constrainedSize.width.toFloat(), constrainedSize.height.toFloat()))
                Constraints(
                    minWidth = (width * scale.scaleX).toInt(),
                    maxWidth = (width * scale.scaleX).toInt(),
                    minHeight = (height * scale.scaleY).toInt(),
                    maxHeight = (height * scale.scaleY).toInt(),
                )
            }
        }

        val placeable = measurable.measure(wrappedConstraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }
}

private operator fun Size.times(scale: ScaleFactor): IntSize {
    return IntSize((width * scale.scaleX).toInt(), (height * scale.scaleY).toInt())
}
