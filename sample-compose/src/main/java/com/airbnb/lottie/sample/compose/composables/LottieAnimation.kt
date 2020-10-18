package com.airbnb.lottie.sample.compose.composables

import android.util.Log
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
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.TimeUnit
import androidx.compose.runtime.getValue

sealed class LottieAnimationSpec {
    class RawRes(@androidx.annotation.RawRes val resId: Int) : LottieAnimationSpec()
    class Url(val url: String) : LottieAnimationSpec()
}

@Composable
fun rememberLottieAnimationState(
    autoPlay: Boolean = true,
    repeatCount: Int = 0
): LottieAnimationController {
    return remember(repeatCount, autoPlay) {
        LottieAnimationController(LottieAnimationState(repeatCount = repeatCount, isPlaying = autoPlay))
    }
}

data class LottieAnimationState(
    val progress: Float = 0f,
    val isPlaying: Boolean = false,
    val repeatCount: Int = 0
)

class LottieAnimationController(initialState: LottieAnimationState = LottieAnimationState()) {
    private var _progress = mutableStateOf(0f)
    val progress: Float by _progress
    var isPlaying by mutableStateOf(false)
    var repeatCount by mutableStateOf(0)

    internal val updateProgressChannel = Channel<Float>(Channel.CONFLATED)

    fun setProgress(progress: Float) {
        updateProgressChannel.offer(progress)
    }

    internal fun updateProgress(progress: Float) {
        _progress.value = progress
    }

    fun toggleIsPlaying() {
        isPlaying = !isPlaying
    }
}

@Composable
fun LottieAnimation(
    spec: LottieAnimationSpec,
    animationController: LottieAnimationController = remember { LottieAnimationController() },
    modifier: Modifier = Modifier
) {
    val context = ContextAmbient.current
    var composition: LottieComposition? by remember { mutableStateOf(null) }
    onCommit(spec) {
        var isDisposed = false
        val task = when(spec) {
            is LottieAnimationSpec.RawRes -> LottieCompositionFactory.fromRawRes(context, spec.resId)
            is LottieAnimationSpec.Url -> LottieCompositionFactory.fromUrl(context, spec.url)
        }
        task.addListener { c ->
            if (!isDisposed) composition = c
        }.addFailureListener { e ->
            Log.d("Gabe", "Animation failed to load", e)
        }
        onDispose {
            isDisposed = true
        }
    }

    LottieAnimation(composition, animationController, modifier)
}

@Composable
fun LottieAnimation(
    composition: LottieComposition?,
    animationController: LottieAnimationController,
    modifier: Modifier = Modifier
) {
    val drawable = remember { LottieDrawable() }
    val isStarted by isStarted()
    val isPlaying = animationController.isPlaying && isStarted
    var progress by remember { mutableStateOf(0f) }

    onCommit(composition) {
        drawable.composition = composition
    }

    LaunchedTask(animationController.updateProgressChannel) {
        for (p in animationController.updateProgressChannel) {
            progress = p
            animationController.updateProgress(progress)
        }
    }

    LaunchedTask(composition, isPlaying) {
        if (!isPlaying || composition == null) return@LaunchedTask
        var repeatCount = 0
        if (isPlaying && progress == 1f) progress = 0f
        var lastFrameTime = withFrameNanos { it }
        while (true) {
            withFrameNanos { frameTime ->
                val dTime = (frameTime - lastFrameTime) / TimeUnit.MILLISECONDS.toNanos(1).toFloat()
                lastFrameTime = frameTime
                val dProgress = dTime / composition.duration
                val previousProgress = progress
                progress = (progress + dProgress) % 1f
                if (previousProgress > progress) {
                    repeatCount++
                    if (repeatCount != 0 && repeatCount > animationController.repeatCount) {
                        progress = 1f
                        animationController.isPlaying = false
                    }
                }
                animationController.updateProgress(progress)
            }
        }
    }

    if (composition == null || composition.duration == 0f) return
    drawable.progress = progress

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
fun isStarted(): State<Boolean> {
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
fun Modifier.maintainAspectRatio(composition: LottieComposition?): Modifier {
    composition ?: return this
    return aspectRatio(composition.bounds.width() / composition.bounds.height().toFloat())
}