package com.airbnb.lottie.compose

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.constrain

@Stable
internal fun Modifier.lottieSize(width: Int, height: Int) = this.then(LottieAnimationSizeElement(width, height))

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
            // There is enough space for the width and height so we don't need to constrain.
            constrainedSize.width >= width && constrainedSize.height >= height -> Constraints(
                minWidth = width,
                maxWidth = width,
                minHeight = height,
                maxHeight = height,
            )
            // There is enough space in the width but not height so we need to constrain the height.
            constrainedSize.width >= width -> Constraints(
                minWidth = constrainedSize.height * width / height,
                maxWidth = constrainedSize.height * width / height,
                minHeight = constrainedSize.height,
                maxHeight = constrainedSize.height,
            )
            // There is enough space in the height but not width so we need to constrain the width.
            constrainedSize.height >= height -> Constraints(
                minWidth = constrainedSize.width,
                maxWidth = constrainedSize.width,
                minHeight = constrainedSize.width * height / width,
                maxHeight = constrainedSize.width * height / width,
            )
            else -> {
                val constraintAspectRation = constrainedSize.height / constrainedSize.width.toFloat()
                val aspectRatio = height / width.toFloat()
                if (constraintAspectRation >= aspectRatio) {
                    Constraints(
                        minWidth = constrainedSize.width,
                        maxWidth = constrainedSize.width,
                        minHeight = constrainedSize.width * height / width,
                        maxHeight = constrainedSize.width * height / width,
                    )
                } else {
                    Constraints(
                        minWidth = constrainedSize.height * width / height,
                        maxWidth = constrainedSize.height * width / height,
                        minHeight = constrainedSize.height,
                        maxHeight = constrainedSize.height,
                    )
                }
            }
        }


        val placeable = measurable.measure(wrappedConstraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }
}
