package com.airbnb.lottie.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.skottie.Animation
import org.jetbrains.skia.sksg.InvalidationController
import kotlin.coroutines.cancellation.CancellationException


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

    LaunchedEffect(spec) {
        when (spec) {
            is LottieCompositionSpec.JsonString -> {
                result.tryCompleteFromJsonString(spec.jsonString)
            }
        }
    }

    return result
}

private suspend fun LottieCompositionResultImpl.tryCompleteFromJsonString(jsonString: String) {
    return withContext(Dispatchers.Default) {
        try {
            val composition = LottieComposition(
                animation = Animation.makeFromString(jsonString),
                lottieData = LottieCompositionParser.parse(jsonString)
            )
            complete(composition)
        } catch (c: CancellationException) {
            throw c
        } catch (t: Throwable) {
            completeExceptionally(t)
        }
    }
}
