package com.airbnb.lottie.compose

import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.AmbientContext
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import java.io.FileInputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
import kotlin.math.floor


/**
 * TODO: add error handling
 */
@Composable
fun rememberLottieComposition(spec: LottieAnimationSpec): LottieCompositionResult {
    val context = AmbientContext.current
    var result: LottieCompositionResult by remember { mutableStateOf(LottieCompositionResult.Loading) }
    DisposableEffect(spec) {
        var isDisposed = false
        val task = when (spec) {
            is LottieAnimationSpec.RawRes -> LottieCompositionFactory.fromRawRes(context, spec.resId)
            is LottieAnimationSpec.Url -> LottieCompositionFactory.fromUrl(context, spec.url)
            is LottieAnimationSpec.File -> {
                val fis = FileInputStream(spec.fileName)
                when {
                    spec.fileName.endsWith("zip") -> LottieCompositionFactory.fromZipStream(ZipInputStream(fis), spec.fileName)
                    else -> LottieCompositionFactory.fromJsonInputStream(fis, spec.fileName)
                }
            }
            is LottieAnimationSpec.Asset -> LottieCompositionFactory.fromAsset(context, spec.assetName)
        }
        task.addListener { c ->
            if (!isDisposed) result = LottieCompositionResult.Success(c)
        }.addFailureListener { e ->
            if (!isDisposed) {
                result = LottieCompositionResult.Fail(e)
            }
        }
        onDispose {
            isDisposed = true
        }
    }
    return result
}

@Composable
fun LottieAnimation(
    spec: LottieAnimationSpec,
    modifier: Modifier = Modifier,
    animationState: LottieAnimationState = rememberLottieAnimationState(autoPlay = true),
) {
    val composition = rememberLottieComposition(spec)
    LottieAnimation(composition, modifier, animationState)
}

@Composable
fun LottieAnimation(
    compositionResult: LottieCompositionResult,
    modifier: Modifier = Modifier,
    animationState: LottieAnimationState = rememberLottieAnimationState(autoPlay = true),
) {
    LottieAnimation(compositionResult(), animationState, modifier)
}

@Composable
fun LottieAnimation(
    composition: LottieComposition?,
    state: LottieAnimationState,
    modifier: Modifier = Modifier,
) {
    val drawable = remember {
        LottieDrawable().apply {
            enableMergePathsForKitKatAndAbove(true)
        }
    }

    SideEffect {
        drawable.composition = composition
    }

    // TODO: handle min/max frame setting

    LaunchedEffect(composition, state.isPlaying) {
        if (!state.isPlaying || composition == null) return@LaunchedEffect
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
                val frame = floor(lerp(drawable.minFrame, drawable.maxFrame, state.progress)).toInt()
                state.updateFrame(frame)
            }
        }
    }

    if (composition == null || composition.duration == 0f) return
    SideEffect {}

    Canvas(
        modifier = Modifier
            .maintainAspectRatio(composition)
            .then(modifier)
    ) {
        drawIntoCanvas { canvas ->
            drawable.progress = state.progress
            drawable.setOutlineMasksAndMattes(state.outlineMasksAndMattes)
            drawable.isApplyingOpacityToLayersEnabled = state.applyOpacityToLayers
            withTransform({
                scale(size.width / composition.bounds.width().toFloat(), size.height / composition.bounds.height().toFloat(), Offset.Zero)
            }) {
                drawable.draw(canvas.nativeCanvas)
            }
        }
    }
}

private fun Modifier.maintainAspectRatio(composition: LottieComposition?): Modifier {
    composition ?: return this
    return this.then(aspectRatio(composition.bounds.width() / composition.bounds.height().toFloat()))
}

private fun lerp(a: Float, b: Float, @FloatRange(from = 0.0, to = 1.0) percentage: Float) =  a + percentage * (b - a)
