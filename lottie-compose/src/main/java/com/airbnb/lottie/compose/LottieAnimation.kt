package com.airbnb.lottie.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.*
import androidx.compose.runtime.dispatch.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.LifecycleOwnerAmbient
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.TimeUnit
import androidx.compose.runtime.getValue
import androidx.compose.ui.util.lerp
import com.airbnb.lottie.*
import com.airbnb.lottie.utils.Logger
import java.io.FileInputStream
import java.util.zip.ZipInputStream
import kotlin.math.floor


/**
 * TODO: add error handling
 */
@Composable
fun rememberLottieComposition(spec: LottieAnimationSpec): LottieCompositionResult {
    val context = ContextAmbient.current
    var result: LottieCompositionResult by remember { mutableStateOf(LottieCompositionResult.Loading) }
    onCommit(spec) {
        var isDisposed = false
        val task = when(spec) {
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
                Logger.error("Failed to parse composition.", e)
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
    animationState: LottieAnimationState = rememberLottieAnimationState(autoPlay = true),
    modifier: Modifier = Modifier,
) {
    val composition = rememberLottieComposition(spec)
    LottieAnimation(composition, animationState, modifier)
}

@Composable
fun LottieAnimation(
    compositionResult: LottieCompositionResult,
    animationState: LottieAnimationState = rememberLottieAnimationState(autoPlay = true),
    modifier: Modifier = Modifier,
) {
    LottieAnimation(compositionResult(), animationState, modifier)
}

@Composable
fun LottieAnimation(
    composition: LottieComposition?,
    state: LottieAnimationState,
    modifier: Modifier = Modifier
) {
    val drawable = remember {
        LottieDrawable().apply {
            enableMergePathsForKitKatAndAbove(true)
        }
    }
    val isStarted by isStarted()
    val isPlaying = state.isPlaying && isStarted

    onCommit(composition) {
        drawable.composition = composition
    }

    // TODO: handle min/max frame setting

    LaunchedTask(composition, isPlaying) {
        if (!isPlaying || composition == null) return@LaunchedTask
        var repeatCount = 0
        if (isPlaying && state.progress == 1f) state.progress = 0f
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
    drawable.progress = state.progress
    drawable.setOutlineMasksAndMattes(state.outlineMasksAndMattes)
    drawable.isApplyingOpacityToLayersEnabled = state.applyOpacityToLayers

    Canvas(
        modifier = Modifier
            .maintainAspectRatio(composition)
            .then(modifier)
    ) {
        drawIntoCanvas { canvas ->
            withTransform({
                scale(size.width / composition.bounds.width().toFloat(), size.height / composition.bounds.height().toFloat(), Offset.Zero)
            }) {
                drawable.draw(canvas.nativeCanvas)
            }
        }
    }
}

@Composable
private fun isStarted(): State<Boolean> {
    val state = remember { mutableStateOf(false) }
    val lifecycleOwner = LifecycleOwnerAmbient.current
    onCommit(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                state.value = true
            }

            override fun onStop(owner: LifecycleOwner) {
                state.value = false
            }
        })
    }
    return state
}

@Composable
private fun Modifier.maintainAspectRatio(composition: LottieComposition?): Modifier {
    composition ?: return this
    return this.then(aspectRatio(composition.bounds.width() / composition.bounds.height().toFloat()))
}
