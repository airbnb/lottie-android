package com.airbnb.lottie.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.FileInputStream
import java.util.zip.ZipInputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Takes a [LottieCompositionSpec], attempts to load and parse the animation, and returns a [LottieCompositionResult].
 *
 * [LottieCompositionResult] allows you to explicitly check for loading, failures, call
 * [LottieCompositionResult.await], or invoke it like a function to get the nullable composition.
 *
 * [LottieCompositionResult] implements State<LottieComposition?> so if you don't need the full result class,
 * you can use this function like:
 * ```
 * val compositionResult: LottieCompositionResult = lottieComposition(spec)
 * // or...
 * val composition: State<LottieComposition?> by lottieComposition(spec)
 * ```
 *
 * @param spec The [LottieCompositionSpec] that defines which LottieComposition should be loaded.
 * @param onRetry An optional callback that will be called if loading the animation fails.
 *                It is passed the failed count (the number of times it has failed) and the exception
 *                from the previous attempt to load the composition. [onRetry] is a suspending function
 *                so you can do things like add a backoff delay or await an internet connection before
 *                retrying again. [rememberLottieRetrySignal] can be used to handle explicit retires.
 */
@Composable
fun rememberLottieComposition(
    spec: LottieCompositionSpec,
    onRetry: suspend (failCount: Int, previousException: Throwable) -> Boolean = { _, _ -> false },
): LottieCompositionResult {
    val context = LocalContext.current
    val result by remember(spec) { mutableStateOf(LottieCompositionResultImpl()) }
    LaunchedEffect(spec) {
        var exception: Throwable? = null
        var failedCount = 0
        while (!result.isSuccess && (failedCount == 0 || onRetry(failedCount, exception!!))) {
            try {
                result.complete(lottieComposition(context, spec))
            } catch (e: Throwable) {
                exception = e
                failedCount++
            }
        }
        if (!result.isComplete && exception != null) {
            result.completeExceptionally(exception)
        }
    }
    return result
}

private suspend fun lottieComposition(
    context: Context,
    spec: LottieCompositionSpec
): LottieComposition = suspendCancellableCoroutine { cont ->
    val task = when (spec) {
        is LottieCompositionSpec.RawRes -> LottieCompositionFactory.fromRawRes(context, spec.resId)
        is LottieCompositionSpec.Url -> LottieCompositionFactory.fromUrl(context, spec.url, null)
        is LottieCompositionSpec.File -> {
            val fis = FileInputStream(spec.fileName)
            when {
                spec.fileName.endsWith("zip") -> LottieCompositionFactory.fromZipStream(ZipInputStream(fis), spec.fileName)
                else -> LottieCompositionFactory.fromJsonInputStream(fis, spec.fileName)
            }
        }
        is LottieCompositionSpec.Asset -> LottieCompositionFactory.fromAsset(context, spec.assetName)
        is LottieCompositionSpec.JsonString -> LottieCompositionFactory.fromJsonString(spec.jsonString, spec.cacheKey)
    }
    task.addListener { c ->
        if (!cont.isCompleted) cont.resume(c)
    }.addFailureListener { e ->
        if (!cont.isCompleted) cont.resumeWithException(e)
    }
}
