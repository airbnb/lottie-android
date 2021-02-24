package com.airbnb.lottie.compose.renderer

import android.graphics.PointF
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.airbnb.lottie.model.animatable.AnimatableTransform
import com.airbnb.lottie.model.content.ShapeGroup
import com.airbnb.lottie.model.layer.Layer
import com.airbnb.lottie.value.Keyframe
import kotlin.properties.Delegates

@Composable
fun rememberTransform(layer: Layer, progress: Float): ComposeLottieTransform {
    val animatableTransform = remember(layer) { layer.transform }
    return rememberTransform(animatableTransform, progress)
}

@Composable
fun rememberTransform(shapeGroup: ShapeGroup, progress: Float): ComposeLottieTransform {
    val animatableTransform = remember(shapeGroup) { shapeGroup.items.lastOrNull() as? AnimatableTransform }
    return rememberTransform(animatableTransform, progress)
}

@Composable
fun rememberTransform(transform: AnimatableTransform?, progress: Float): ComposeLottieTransform {
    // TODO: look into optimizations here.
    val composeTransform = remember(transform) { ComposeLottieTransform(transform) }
    composeTransform.progress = progress
    return composeTransform
}

class ComposeLottieTransform(private val transform: AnimatableTransform?) {
    var progress by Delegates.observable(0f) { _, oldValue, newValue ->
        if (oldValue == newValue) return@observable
        updatePosition(newValue)
    }

    private var positionProgress = 0f
    private var positionKeyframeIndex = 0
    private var _position = mutableStateOf(PointF())
    val position: PointF by _position

    val isIdentity get() = transform == null || position.equals(0f, 0f)

    private fun updatePosition(progress: Float) {
        if (positionProgress == progress) return
        val keyframes = transform?.position?.keyframes ?: return

        val keyframe = keyframes.findKeyframeIndex(progress, positionKeyframeIndex)
        val linearProgress = lerp(keyframe.startProgress, keyframe.endProgress, progress)
        val interpolatedProgress = when (val i = keyframe.interpolator) {
            null -> linearProgress
            else -> i.getInterpolation(linearProgress)
        }
        _position.value.set(
            lerp(keyframe.startValue?.x ?: 0f, keyframe.endValue?.x ?: 0f, interpolatedProgress),
            lerp(keyframe.startValue?.y ?: 0f, keyframe.endValue?.y ?: 0f, interpolatedProgress),
        )
    }
}

private fun <T> List<Keyframe<T>>.findKeyframeIndex(progress: Float, currentKeyframeIndex: Int): Keyframe<T> {
    var keyframe = get(currentKeyframeIndex)
    if (keyframe.containsProgress(progress)) return keyframe
    // TODO: if speed is reversed, flip these two.
    for (i in currentKeyframeIndex until size) {
        keyframe = get(i)
        if (keyframe.containsProgress(progress)) return keyframe
    }
    for (i in 0 until currentKeyframeIndex) {
        keyframe = get(i)
        if (keyframe.containsProgress(progress)) return keyframe
    }
    return keyframe
}