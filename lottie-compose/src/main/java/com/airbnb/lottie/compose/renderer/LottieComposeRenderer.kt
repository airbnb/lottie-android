package com.airbnb.lottie.compose.renderer

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.airbnb.lottie.compose.LottieAnimationState
import com.airbnb.lottie.compose.LottieCompositionResult
import com.airbnb.lottie.model.animatable.AnimatableTransform
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
                Layer.LayerType.SHAPE -> ShapeLayer(layer)
                else -> Unit
            }
        }
    }
}

@Composable
fun ShapeLayer(layer: Layer) {
    val progress = LocalLottieProgress.current
    val animatableTransform: AnimatableTransform = remember(layer) { layer.shapes.firstInstanceOf() }
    val transform by transformMatrix(animatableTransform)

    Canvas(
        modifier = Modifier
            .withTransform(progress, animatableTransform)
    ) {

    }
}