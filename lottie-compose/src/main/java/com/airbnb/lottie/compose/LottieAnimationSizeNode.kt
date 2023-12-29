package com.airbnb.lottie.compose

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
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
internal fun Modifier.lottieSize(
    width: Int,
    height: Int,
) = this.then(LottieAnimationSizeElement(width, height))

internal data class LottieAnimationSizeElement(
    val width: Int,
    val height: Int,
) : ModifierNodeElement<LottieAnimationSizeNode>() {
    override fun create(): LottieAnimationSizeNode {
        return LottieAnimationSizeNode(width, height)
    }

    override fun update(node: LottieAnimationSizeNode) {
        node.width = width
        node.height = height
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "Lottie Size"
        properties["width"] = width
        properties["height"] = height
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LottieAnimationSizeElement) return false

        if (width != other.width) return false
        if (height != other.height) return false
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
) : Modifier.Node(), LayoutModifierNode {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val constrainedSize = constraints.constrain(IntSize(width, height))
        val wrappedConstraints = when {
            constraints.maxHeight == Constraints.Infinity && constraints.maxWidth != Constraints.Infinity -> Constraints(
                minWidth = constrainedSize.width,
                maxWidth = constrainedSize.width,
                minHeight = constrainedSize.width * height / width,
                maxHeight = constrainedSize.width * height / width,
            )
            constraints.maxWidth == Constraints.Infinity && constraints.maxHeight != Constraints.Infinity -> Constraints(
                minWidth = constrainedSize.height * width / height,
                maxWidth = constrainedSize.height * width / height,
                minHeight = constrainedSize.height,
                maxHeight = constrainedSize.height,
            )
            else -> Constraints(
                minWidth = constrainedSize.width,
                maxWidth = constrainedSize.width,
                minHeight = constrainedSize.height,
                maxHeight = constrainedSize.height,
            )
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
