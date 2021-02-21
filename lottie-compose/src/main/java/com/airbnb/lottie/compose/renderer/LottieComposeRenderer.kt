package com.airbnb.lottie.compose.renderer

import android.graphics.PointF
import android.util.Log
import androidx.compose.animation.animate
import androidx.compose.animation.core.DurationBasedAnimationSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimationState
import com.airbnb.lottie.compose.LottieCompositionResult
import com.airbnb.lottie.model.content.*
import com.airbnb.lottie.model.layer.Layer
import com.airbnb.lottie.value.Keyframe
import java.util.concurrent.TimeUnit
import kotlin.math.floor

val LocalLottieProgress = compositionLocalOf { 0f }

@Composable
fun ComposeLottieAnimation(
    compositionResult: LottieCompositionResult,
    state: LottieAnimationState,
    modifier: Modifier = Modifier,
) {
    if (compositionResult !is LottieCompositionResult.Success) return
    val composition = compositionResult.composition

    LaunchedEffect(composition, state.isPlaying) {
        if (!state.isPlaying) return@LaunchedEffect
        var repeatCount = 0
        if (state.isPlaying && state.progress == 1f) state.progress = 0f
        var lastFrameTime = withFrameNanos { it }
        while (true) {
            withFrameNanos { frameTime ->
                val dTime = (frameTime - lastFrameTime) / TimeUnit.MILLISECONDS.toNanos(1).toFloat()
                lastFrameTime = frameTime
                val dProgress = (dTime * state.speed) / composition.duration
                val previousProgress = state.progress
                state.progress = (state.progress + dProgress) % 1f
                if (previousProgress > state.progress) {
                    repeatCount++
                    if (repeatCount != 0 && repeatCount > state.repeatCount) {
                        state.progress = 1f
                        state.isPlaying = false
                    }
                }
                // TODO: handle min/max frames.
                val frame = floor(lerp(composition.startFrame, composition.endFrame, state.progress)).toInt()
                state.updateFrame(frame)
            }
        }
    }

    Providers(
        LocalLottieProgress provides state.progress
    ) {
        composition.layers.forEach { layer ->
            when (layer.layerType) {
                Layer.LayerType.SHAPE -> Image(
                    shapeLayerPainter(composition, layer),
                    contentDescription = null,
                    modifier = modifier,
                )
                else -> Unit
            }
        }
    }
}

@Composable
fun shapeLayerPainter(composition: LottieComposition, layer: Layer): VectorPainter {
    val transform = rememberTransform(layer)
    return rememberVectorPainter(
        defaultWidth = composition.bounds.width().dp,
        defaultHeight = composition.bounds.height().dp,
        viewportWidth = composition.bounds.width().toFloat(),
        viewportHeight = composition.bounds.height().toFloat(),
    ) { viewportWidth, viewportHeight ->
        val clipPathData = getMaskPathData(layer.masks)
        Log.d("Gabe", "clipPathData hash ${clipPathData.hashCode()} identity ${System.identityHashCode(clipPathData)}")
        Group(
            name = layer.layerName,
            translationX = transform.position.x,
            translationY = transform.position.y,
            scaleX = composition.bounds.width() / viewportWidth,
            scaleY = composition.bounds.height() / viewportHeight,
            clipPathData = clipPathData,
        ) {
            layer.shapes.forEach { shapeModel ->
                when (shapeModel) {
                    is ShapeGroup -> ComposeShapeGroup(shapeModel)
                    else -> Log.d("Gabe", "Don't know how to draw ${shapeModel::class.simpleName}")
                }
            }
        }
    }
}

@Composable
fun ComposeShapeGroup(shapeGroup: ShapeGroup) {
    if (shapeGroup.isHidden || shapeGroup.items.isEmpty()) return
    val transform = rememberTransform(shapeGroup)
    val pathData = remember { mutableListOf<PathNode>() }
    Group(
        name = shapeGroup.name,
        translationX = transform.position.x,
        translationY = transform.position.y,
    ) {
        // Reuse the list and clear it so that the backing array doesn't need to be recreated.
        pathData.clear()
        pathData += PathData {
            moveTo(380f, 0f)
            lineTo(400f, 0f)
            lineTo(400f, 20f)
            lineTo(380f, 20f)
            lineTo(380f, 0f)
            close()
        }
        for (model in shapeGroup.items) {
            when (model) {
                is RectangleShape -> {
                    pathData += rectanglePathData(model)
                }
                is ShapeFill -> {
                    ComposeShapeFill(model, pathData)
                }
            }
        }
    }
}

@Composable
fun rectanglePathData(shape: RectangleShape): List<PathNode> {
    val size = remember { PointF() }
    val (sizeKeyframe, interpolatedProgress) = shape.size.keyframes.rememberKeyframeProgress()
    size.set(
        lerp(sizeKeyframe?.startValue?.x ?: 0f, sizeKeyframe?.endValue?.x ?: 0f, interpolatedProgress),
        lerp(sizeKeyframe?.startValue?.y ?: 0f, sizeKeyframe?.endValue?.y ?: 0f, interpolatedProgress),
    )

    val halfWidth = size.x / 2f
    val halfHeight = size.y / 2f

    return PathData {
        moveTo(-halfWidth, -halfHeight)
        lineTo(halfWidth, -halfHeight)
        lineTo(halfWidth, halfHeight)
        lineTo(-halfWidth, halfHeight)
        lineTo(-halfWidth, -halfHeight)
        close()
    }
}

@Composable
fun ComposeShapeFill(fill: ShapeFill, pathData: List<PathNode>) {
    // TODO
    val colorAnimation = fill.color?.keyframes ?: return
    Path(
        pathData,
        // TODO: use the real color
        fill = SolidColor(Color.Red),
    )
}

@Composable
fun getMaskPathData(masks: List<Mask>): List<PathNode> {
    // TODO: figure out how to reuse the mutable list.
    // https://issuetracker.google.com/issues/180774141
    val pathNodes = mutableListOf<PathNode>()
    val allShapeData = remember(masks.size) { List(masks.size) { ShapeData() } }
    pathNodes.clear()
    for (i in masks.indices) {
        val mask = masks[i]
        val (keyframe, progress) = mask.maskPath.keyframes.rememberKeyframeProgress()
        val shapeData = allShapeData[i]
        val startData = keyframe?.startValue
        val endData = keyframe?.endValue
        if (startData != null && endData != null) {
            shapeData.interpolateBetween(startData, endData, progress)
            pathNodes += PathData {
                moveTo(shapeData.initialPoint.x, shapeData.initialPoint.y)
                for (curveData in shapeData.curves) {
                    curveTo(
                        curveData.controlPoint1.x,
                        curveData.controlPoint1.y,
                        curveData.controlPoint2.x,
                        curveData.controlPoint2.y,
                        curveData.vertex.x,
                        curveData.vertex.y,
                    )
                }
            }
        }
    }
    return pathNodes
}

data class KeyframeProgress<T>(var keyframe: Keyframe<T>? = null, var progress: Float = 0f)

@Composable
fun <T> List<Keyframe<T>>.rememberKeyframeProgress(): KeyframeProgress<T> {
    val progress = LocalLottieProgress.current
    val value = remember { KeyframeProgress<T>() }

    val keyframe = firstOrNull { it.containsProgress(progress) } ?: return value
    value.keyframe = keyframe
    val linearSizeProgress = lerp(keyframe.startProgress, keyframe.endProgress, progress)
    value.progress = when (val i = keyframe.interpolator) {
        null -> linearSizeProgress
        else -> i.getInterpolation(linearSizeProgress)
    }

    return value
}