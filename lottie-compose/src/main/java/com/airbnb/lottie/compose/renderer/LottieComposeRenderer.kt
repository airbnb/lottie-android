package com.airbnb.lottie.compose.renderer

import android.graphics.PointF
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimationState
import com.airbnb.lottie.compose.LottieCompositionResult
import com.airbnb.lottie.model.animatable.AnimatableTransform
import com.airbnb.lottie.model.content.RectangleShape
import com.airbnb.lottie.model.content.ShapeFill
import com.airbnb.lottie.model.content.ShapeGroup
import com.airbnb.lottie.model.layer.Layer
import java.util.concurrent.TimeUnit
import kotlin.math.floor

val LocalLottieProgress = compositionLocalOf { 0f }

@Composable
fun ComposeLottieAnimation(compositionResult: LottieCompositionResult, state: LottieAnimationState) {
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
                    // TODO: use a different size
                    modifier = Modifier.size(256.dp)
                )
                else -> Unit
            }
        }
    }
}

@Composable
fun shapeLayerPainter(composition: LottieComposition, layer: Layer): VectorPainter {
    val progress = LocalLottieProgress.current
    val animatableTransform = remember(layer) { layer.shapes.firstOrNull { it is AnimatableTransform } as? AnimatableTransform }
    val transform = LayerTransform(animatableTransform)
    LaunchedEffect(progress) {
        transform.progress = progress
    }

    return rememberVectorPainter(
        defaultWidth = 256.dp,
        defaultHeight = 256.dp,
    ) { viewportWidth, viewportHeight ->
        Group(
            name = layer.layerName,
            translationX = transform.position.x,
            translationY = transform.position.y,
            scaleX = viewportWidth / composition.bounds.width(),
            scaleY = viewportHeight / composition.bounds.height(),
        ) {
            PathData {
                layer.shapes.forEach { shapeModel ->
                    when (shapeModel) {
                        is ShapeGroup -> ComposeShapeGroup(shapeModel)
                        else -> Log.d("Gabe", "Don't know how to draw ${shapeModel::class.simpleName}")
                    }
                }
            }
        }
    }
}

@Composable
fun ComposeShapeGroup(shapeGroup: ShapeGroup) {
    if (shapeGroup.isHidden || shapeGroup.items.isEmpty()) return
    val transform = remember(shapeGroup) { LayerTransform(shapeGroup.items.lastOrNull() as? AnimatableTransform) }
    val pathData = remember { mutableListOf<PathNode>() }
    val progress = LocalLottieProgress.current
    LaunchedEffect(progress) {
        transform.progress = progress
    }
    pathData.clear()
    Group(
        name = shapeGroup.name,
        translationX = transform.position.x,
        translationY = transform.position.y,
    ) {
        for (model in shapeGroup.items) {
            when (model) {
                is RectangleShape -> {
                    pathData += rectanglePathData(model)
                }
                is ShapeFill -> {
                    ComposeShapeFill(model, pathData)
                }
            }
            Log.d("Gabe", "Drawing ${model::class.simpleName} $model")
        }
    }
}

@Composable
fun rectanglePathData(shape: RectangleShape): List<PathNode> {
    val progress = LocalLottieProgress.current
    // TODO: optimize this
    val size = remember { PointF() }
    val sizeKeyframe = shape.size.keyframes.firstOrNull { it.containsProgress(progress) } ?: return emptyList()
    val linearSizeProgress = lerp(sizeKeyframe.startProgress, sizeKeyframe.endProgress, progress)
    val interpolatedProgress = when (val i = sizeKeyframe.interpolator) {
        null -> linearSizeProgress
        else -> i.getInterpolation(linearSizeProgress)
    }
    size.set(
        lerp(sizeKeyframe.startValue?.x ?: 0f, sizeKeyframe.endValue?.x ?: 0f, interpolatedProgress),
        lerp(sizeKeyframe.startValue?.y ?: 0f, sizeKeyframe.endValue?.y ?: 0f, interpolatedProgress),
    )

    return PathData {
        moveTo(0f, 0f)
        lineTo(size.x, 0f)
        lineTo(size.x, size.y)
        lineTo(0f, size.y)
        lineTo(0f, 0f)
        close()
    }
}

@Composable
fun ComposeShapeFill(fill: ShapeFill, pathData: List<PathNode>) {
    Log.d("Gabe", "Drawing fill with ${pathData.size} nodes")
    Path(
        pathData,
        // TODO: use the real color
        fill = SolidColor(Color.Red),
    )
}