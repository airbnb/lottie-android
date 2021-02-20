package com.airbnb.lottie.compose.renderer

import android.graphics.PointF
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.graphicsLayer
import com.airbnb.lottie.animation.keyframe.PathKeyframe
import com.airbnb.lottie.model.animatable.AnimatableTransform
import com.airbnb.lottie.model.layer.Layer
import com.airbnb.lottie.value.Keyframe
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.airbnb.lottie.value.ScaleXY
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

@Composable
private fun <T> List<Keyframe<T>>.asState(
    progress: Float,
    defaultValue: T? = null,
    calculator: (keyframe: Keyframe<T>, interpolatedProgress: Float) -> T,
): T {
    if (isEmpty()) {
        return remember { defaultValue ?: error("You must specify a default value if the keyframes are empty.") }
    }

    var lastCalculatedProgress by remember { mutableStateOf(0f) }
    var value by remember { mutableStateOf(defaultValue ?: calculator(first(), 0f)) }
    if (lastCalculatedProgress == progress) return value
    lastCalculatedProgress = progress

    var keyframeIndex by remember { mutableStateOf(0) }
    val newKeyframeIndex = findKeyframeIndex(progress, keyframeIndex)
    val keyframe = get(keyframeIndex)
    // The keyframe didn't change and it is static.
    if (keyframeIndex == newKeyframeIndex && keyframe.isStatic) return value

    val interpolatedProgress by keyframe.getInterpolatedProgressIntoKeyframe(progress)
    value = calculator(keyframe, interpolatedProgress)
    return value
}

@Composable
fun List<Keyframe<Float>>.asFloatState(progress: Float, defaultValue: Float = 0f): Float {
    return asState(progress, defaultValue) { keyframe, interpolatedProgress ->
        lerp(keyframe.startValue ?: 0f, keyframe.endValue ?: 0f, interpolatedProgress)
    }
}

@Composable
fun List<Keyframe<Int>>.asIntState(progress: Float, defaultValue: Int = 0): Int {
    return asState(progress, defaultValue) { keyframe, interpolatedProgress ->
        lerp(keyframe.startValue ?: 0, keyframe.endValue ?: 0, interpolatedProgress)
    }
}

@Composable
fun List<Keyframe<ScaleXY>>.asScaleXYState(progress: Float, defaultValue: ScaleXY = ScaleXY()): ScaleXY {
    val outPoint = remember { ScaleXY() }
    return asState(progress, defaultValue) { keyframe, interpolatedProgress ->
        outPoint.set(
            lerp(keyframe.startValue?.scaleX ?: 0f, keyframe.endValue?.scaleX ?: 0f, interpolatedProgress),
            lerp(keyframe.startValue?.scaleY ?: 0f, keyframe.endValue?.scaleY ?: 0f, interpolatedProgress),
        )
        outPoint
    }
}

@Composable
fun List<Keyframe<PointF>>.asPointFState(progress: Float, defaultValue: PointF = PointF()): PointF {
    val outPoint = remember { PointF() }
    return asState(progress, defaultValue) { keyframe, interpolatedProgress ->
        outPoint.set(
            lerp(keyframe.startValue?.x ?: 0f, keyframe.endValue?.x ?: 0f, interpolatedProgress),
            lerp(keyframe.startValue?.y ?: 0f, keyframe.endValue?.y ?: 0f, interpolatedProgress),
        )
        outPoint
    }
}

@Composable
fun List<PathKeyframe>.asPathToPointState(progress: Float, defaultValue: PointF = PointF()): State<PointF> {
    if (isEmpty()) {
        return remember { mutableStateOf(defaultValue) }
    }

    var lastCalculatedProgress by remember { mutableStateOf(0f) }
    var value = remember { mutableStateOf(defaultValue ) }
    if (lastCalculatedProgress == progress) return value
    lastCalculatedProgress = progress

    var keyframeIndex by remember { mutableStateOf(0) }
    val newKeyframeIndex = findKeyframeIndex(progress, keyframeIndex)
    val keyframe = get(keyframeIndex)
    // The keyframe didn't change and it is static.
    if (keyframeIndex == newKeyframeIndex && keyframe.isStatic) return value

    val interpolatedProgress by keyframe.getInterpolatedProgressIntoKeyframe(progress)
    val path = keyframe.path
    if (path == null) {
        value.value = keyframe.startValue ?: value.value
    } else {
        value.value = pointAlongPath(path, interpolatedProgress)
    }
    return value
}

@Composable
fun pointAlongPath(path: android.graphics.Path, progress: Float): PointF {
    val locs = remember { FloatArray(2) }
    var value by remember { mutableStateOf(PointF()) }
    val pathMeasure = remember { android.graphics.PathMeasure() }
    LaunchedEffect(path) {
        pathMeasure.setPath(path, false)
    }
    LaunchedEffect(path, progress) {
        pathMeasure.getPosTan(progress * pathMeasure.length, locs, null)
    }
    value.set(locs[0], locs[1])
    return value
}

private fun List<Keyframe<*>>.findKeyframeIndex(progress: Float, currentKeyframeIndex: Int): Int {
    if (get(currentKeyframeIndex).containsProgress(progress)) return currentKeyframeIndex
    // TODO: if speed is reversed, flip these two.
    for (i in currentKeyframeIndex until size) {
        if (get(i).containsProgress(progress)) return i
    }
    for (i in 0 until currentKeyframeIndex) {
        if (get(0).containsProgress(progress)) return i
    }
    error("Unable to find keyframe for progress $progress.")
}

@Composable
private fun Keyframe<*>.getInterpolatedProgressIntoKeyframe(progress: Float): State<Float> {
    var interpolatedProgress = remember { mutableStateOf(0f) }
    val linearProgress = lerp(startProgress, endProgress, progress)
    interpolatedProgress.value = when (val i = interpolator) {
        null -> linearProgress
        else -> i.getInterpolation(linearProgress)
    }
    return interpolatedProgress
}

class LayerTransform(layer: Layer) {
    private val transform: AnimatableTransform = layer.shapes.firstInstanceOf()

    var progress by mutableStateOf(0f)

    val position by derivedStateOf { transform.position?.keyframes?.asPointFState(progress) }
}