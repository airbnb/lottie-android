package com.airbnb.lottie.compose

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import java.io.FileInputStream
import java.util.zip.ZipInputStream

/**
 * This is the simplest way to load and parse a [LottieComposition]. It will return null
 * while loading or if failed.
 *
 * If you are loading an animation that might fail such as one from the network, you may want
 * to use [rememberLottieCompositionResult] to distinguish between loading and failure states.
 *
 * If you don't need to explicitly check for the failure case, you may want to use the LottieAnimation
 * overloads that take [LottieCompositionSpec] instead.
 */
@Composable
fun rememberLottieComposition(spec: LottieCompositionSpec): LottieComposition? {
    return rememberLottieCompositionResult(spec)()
}

/**
 * Takes a [LottieCompositionSpec], attempts to load and parse the animation, and returns a [LottieCompositionResult].
 * 
 * In most cases, [rememberLottieComposition] is sufficient but if you want to explicitly track loading or failures,
 * you may use this.
 *
 * You can call [LottieCompositionResult.invoke] as an operator on the result to get a nullable composition. It will
 * be null when loading or after a failure.
 *
 * If you don't need to explicitly check for the failure case, you may want to use the LottieAnimation
 * overloads that take [LottieCompositionSpec] instead.
 */
@Composable
fun rememberLottieCompositionResult(spec: LottieCompositionSpec): LottieCompositionResult {
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