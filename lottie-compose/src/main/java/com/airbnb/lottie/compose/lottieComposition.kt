package com.airbnb.lottie.compose

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.airbnb.lottie.LottieCompositionFactory
import java.io.FileInputStream
import java.util.zip.ZipInputStream

/**
 * Takes a [LottieCompositionSpec], attempts to load and parse the animation, and returns a [LottieCompositionResult].
 *
 * [LottieCompositionResult] allows you to explicitly check for loading, failures, call
 * [LottieCompositionResult.await], or invoke it like a function to get the nullable composition.
 *
 * [LottieCompositionResult] implements State<LottieComposition?> so if you don't need the full result class,
 * you can use this function like:
 * ```
 * val composition by lottieComposition(compositionSpec)
 * ```
 */
@Composable
fun lottieComposition(spec: LottieCompositionSpec): LottieCompositionResult {
    val context = LocalContext.current
    val result: LottieCompositionResult by remember { mutableStateOf(LottieCompositionResult()) }
    DisposableEffect(spec) {
        var isDisposed = false
        val task = when (spec) {
            is LottieCompositionSpec.RawRes -> LottieCompositionFactory.fromRawRes(context, spec.resId)
            is LottieCompositionSpec.Url -> LottieCompositionFactory.fromUrl(context, spec.url)
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
            if (!isDisposed) result.complete(c)
        }.addFailureListener { e ->
            if (!isDisposed) {
                result.completeExceptionally(e)
            }
        }
        onDispose {
            isDisposed = true
        }
    }
    return result
}