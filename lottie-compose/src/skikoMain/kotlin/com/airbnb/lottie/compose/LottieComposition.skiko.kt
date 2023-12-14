package com.airbnb.lottie.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.skottie.Animation
import org.jetbrains.skia.sksg.InvalidationController


actual class LottieComposition internal constructor(
    internal val animation: Animation,
    internal val invalidationController: InvalidationController = InvalidationController(),
    internal val lottieData: LottieData
)

internal actual val LottieComposition.fps: Float
    get() = animation.fPS

internal actual val LottieComposition.durationMillis: Float
    get() = animation.duration * 1000

internal actual val LottieComposition.lastFrame : Float
    get() = animation.outPoint

internal actual fun LottieComposition.marker(markerName : String) : Marker? =
    lottieData.markersMap?.get(markerName)



@Composable
actual fun rememberLottieComposition(spec : LottieCompositionSpec) : LottieCompositionResult {

    val result by remember(spec) {
        mutableStateOf(LottieCompositionResultImpl())
    }

    DisposableEffect(result.value) {
        val old = result.value

        onDispose {
            kotlin.runCatching {
                old?.animation?.close()
                old?.invalidationController?.close()
            }
        }
    }

    LaunchedEffect(spec){
        when (spec){
            is LottieCompositionSpec.JsonString -> {
                withContext(Dispatchers.Default) {
                    try {
                        val composition = parseFromJsonString(spec.jsonString)
                        result.complete(composition)
                    } catch (c: CancellationException) {
                        throw c
                    } catch (t: Throwable) {
                        result.completeExceptionally(t)
                    }
                }
            }
        }
    }

    return result
}

private fun parseFromJsonString(jsonString: String) : LottieComposition {
    return LottieComposition(
        animation = Animation.makeFromString(jsonString),
        lottieData = LottieCompositionParser.parse(jsonString)
    )
}
