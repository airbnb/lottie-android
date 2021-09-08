package com.airbnb.lottie.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import com.airbnb.lottie.LottieComposition

internal fun Modifier.lottieLayout(
    composition: LottieComposition,
): Modifier {
    return this.then(LottieAnimationLayoutModifier(composition))
}

private class LottieAnimationLayoutModifier(
    private val composition: LottieComposition,
) : LayoutModifier {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val wrappedConstraints = modifyConstraints(constraints)
        val placeable = measurable.measure(wrappedConstraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }

    /**
     * If there is a fixed size on either the width or height, use that.
     * If there is not, use the composition size but constraint it to the constraints.
     */
    private fun modifyConstraints(constraints: Constraints): Constraints {
        val width = when {
            constraints.hasFixedWidth -> constraints.maxWidth
            else -> constraints.constrainWidth(composition.bounds.width())
        }
        val height = when {
            constraints.hasFixedHeight -> constraints.maxHeight
            else -> constraints.constrainHeight(composition.bounds.height())
        }
        return Constraints(
            minWidth = width,
            maxWidth = width,
            minHeight = height,
            maxHeight = height,
        )
    }
}