package com.airbnb.lottie.sample.compose.composables

import android.util.Log
import androidx.annotation.RawRes
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.runtime.dispatch.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.LifecycleOwnerAmbient
import androidx.compose.ui.unit.dp
import androidx.lifecycle.whenStarted
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable

@Composable
fun LottieAnimation(
    @RawRes animationRes: Int,
    modifier: Modifier = Modifier
) {
    val context = ContextAmbient.current
    val drawable = remember { LottieDrawable() }
    val animationTime = animationTimeMillis()
    drawable.progress = (animationTime.value / (drawable.composition?.duration ?: 1f)) % 1f
    onCommit(animationRes) {
        var isDisposed = false
        LottieCompositionFactory.fromRawRes(context, animationRes)
            .addListener { c ->
                if (!isDisposed) drawable.composition = c
            }
        onDispose {
            isDisposed = true
        }
    }

    val matrix = remember { Matrix() }

    Canvas(modifier = modifier) {
        drawCanvas { canvas, size ->
            val composition = drawable.composition ?: return@drawCanvas
            withTransform({
                scale(size.width / composition.bounds.width().toFloat(), size.height / composition.bounds.height().toFloat(), 0f, 0f)
            }) {
                Log.d("Gabe", "LottieAnimation: ${size} ${size.width / composition.bounds.width().toFloat()}")
                drawable.draw(canvas.nativeCanvas)
            }
        }
    }
}

@Composable
private fun animationTimeMillis(): State<Long> {
    val millisState = remember { mutableStateOf(0L) }
    val lifecycleOwner = LifecycleOwnerAmbient.current
    launchInComposition {
        val startTime = withFrameMillis { it }
        lifecycleOwner.whenStarted {
            while (true) {
                withFrameMillis { frameTime ->
                    millisState.value = frameTime - startTime
                }
            }
        }
    }
    return millisState
}