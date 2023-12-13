package com.airbnb.lottie.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.skottie.Animation
import org.jetbrains.skia.sksg.InvalidationController


actual class LottieComposition internal constructor(
    internal val animation: Animation,
    internal val invalidationController: InvalidationController = InvalidationController()
)

internal actual val LottieComposition.fps: Float
    get() = animation.fPS

internal actual val LottieComposition.durationMillis: Float
    get() = animation.duration * 1000

internal actual val LottieComposition.lastFrame : Float
    get() = animation.fPS * animation.duration


@Composable
actual fun rememberLottieComposition(spec : LottieCompositionSpec) : LottieCompositionResult {

    val result by remember(spec) {
        mutableStateOf(LottieCompositionResultImpl())
    }

    LaunchedEffect(spec){
        when (spec){
            is LottieCompositionSpec.JsonString -> {
                withContext(Dispatchers.Default) {
                    try {
                        val composition = LottieComposition(Animation.makeFromString(spec.jsonString))
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

//private class LottieCompositionResultImpl(
//    val deferred: CompletableDeferred<LottieComposition>
//) : LottieCompositionResult {
//
//    override var value: LottieComposition? = null
//        internal set
//
//    override var error: Throwable? by mutableStateOf(null)
//        internal set
//
//    override var isLoading: Boolean by mutableStateOf(false)
//        internal set
//
//    override var isComplete: Boolean by mutableStateOf(false)
//        internal set
//
//    override val isFailure: Boolean by derivedStateOf {
//        error != null
//    }
//
//    override val isSuccess: Boolean by derivedStateOf {
//        value != null
//    }
//
//    override suspend fun await(): LottieComposition {
//        return deferred.await()
//    }
//
//}
