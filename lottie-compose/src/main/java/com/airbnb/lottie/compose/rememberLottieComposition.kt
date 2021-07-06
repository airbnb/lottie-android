package com.airbnb.lottie.compose

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieImageAsset
import com.airbnb.lottie.LottieTask
import com.airbnb.lottie.utils.Logger
import com.airbnb.lottie.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.IOException
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
 * The loaded composition will automatically load and set images that are embedded in the json as a base64 string
 * or will load them from assets if an imageAssetsFolder is supplied.
 *
 * @param spec The [LottieCompositionSpec] that defines which LottieComposition should be loaded.
 * @param imageAssetsFolder A subfolder in `src/main/assets` that contains the exported images
 *                          that this composition uses. DO NOT rename any images from your design tool. The
 *                          filenames must match the values that are in your json file.
 * @param onRetry An optional callback that will be called if loading the animation fails.
 *                It is passed the failed count (the number of times it has failed) and the exception
 *                from the previous attempt to load the composition. [onRetry] is a suspending function
 *                so you can do things like add a backoff delay or await an internet connection before
 *                retrying again. [rememberLottieRetrySignal] can be used to handle explicit retires.
 */
@Composable
fun rememberLottieComposition(
    spec: LottieCompositionSpec,
    imageAssetsFolder: String? = null,
    cacheComposition: Boolean = true,
    onRetry: suspend (failCount: Int, previousException: Throwable) -> Boolean = { _, _ -> false },
): LottieCompositionResult {
    val context = LocalContext.current
    val result by remember(spec) { mutableStateOf(LottieCompositionResultImpl()) }
    LaunchedEffect(spec) {
        var exception: Throwable? = null
        var failedCount = 0
        while (!result.isSuccess && (failedCount == 0 || onRetry(failedCount, exception!!))) {
            try {
                val composition = lottieComposition(
                    context,
                    spec,
                    imageAssetsFolder.ensureTrailingSlash(),
                    cacheComposition,
                )
                result.complete(composition)
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
    spec: LottieCompositionSpec,
    imageAssetsFolder: String?,
    cacheComposition: Boolean,
): LottieComposition {
    val task = when (spec) {
        is LottieCompositionSpec.RawRes -> {
            if (cacheComposition) {
                LottieCompositionFactory.fromRawRes(context, spec.resId)
            } else {
                LottieCompositionFactory.fromRawRes(context, spec.resId, null)
            }
        }
        is LottieCompositionSpec.Url -> {
            if (cacheComposition) {
                LottieCompositionFactory.fromUrl(context, spec.url)
            } else {
                LottieCompositionFactory.fromUrl(context, spec.url, null)
            }
        }
        is LottieCompositionSpec.File -> {
            val fis = withContext(Dispatchers.IO) {
                @Suppress("BlockingMethodInNonBlockingContext")
                FileInputStream(spec.fileName)
            }
            when {
                spec.fileName.endsWith("zip") -> LottieCompositionFactory.fromZipStream(ZipInputStream(fis), spec.fileName.takeIf { cacheComposition })
                else -> LottieCompositionFactory.fromJsonInputStream(fis, spec.fileName.takeIf { cacheComposition })
            }
        }
        is LottieCompositionSpec.Asset -> {
            if (cacheComposition) {
                LottieCompositionFactory.fromAsset(context, spec.assetName)
            } else {
                LottieCompositionFactory.fromAsset(context, spec.assetName, null)
            }
        }
        is LottieCompositionSpec.JsonString -> {
            LottieCompositionFactory.fromJsonString(spec.jsonString, spec.cacheKey.takeIf { cacheComposition })
        }
    }

    val composition = task.await()
    loadImagesFromAssets(context, composition, imageAssetsFolder)
    return composition
}

private suspend fun <T> LottieTask<T>.await(): T = suspendCancellableCoroutine { cont ->
    addListener { c ->
        if (!cont.isCompleted) cont.resume(c)
    }.addFailureListener { e ->
        if (!cont.isCompleted) cont.resumeWithException(e)
    }
}

private suspend fun loadImagesFromAssets(
    context: Context,
    composition: LottieComposition,
    imageAssetsFolder: String?,
) {
    if (!composition.hasImages()) {
        return
    }
    withContext(Dispatchers.IO) {
        for (asset in composition.images.values) {
            maybeDecodeBase64Image(asset)
            maybeLoadImageFromAsset(context, asset, imageAssetsFolder)
        }
    }
}

private fun maybeLoadImageFromAsset(
    context: Context,
    asset: LottieImageAsset,
    imageAssetsFolder: String?,
) {
    if (asset.bitmap != null || imageAssetsFolder == null) return
    val filename = asset.fileName
    val inputStream = try {
        context.assets.open(imageAssetsFolder + filename)
    } catch (e: IOException) {
        Logger.warning("Unable to open asset.", e)
        return
    }
    try {
        val opts = BitmapFactory.Options()
        opts.inScaled = true
        opts.inDensity = 160
        var bitmap = BitmapFactory.decodeStream(inputStream, null, opts)
        bitmap = Utils.resizeBitmapIfNeeded(bitmap, asset.width, asset.height)
        asset.bitmap = bitmap
    } catch (e: IllegalArgumentException) {
        Logger.warning("Unable to decode image.", e)
    }
}

private fun maybeDecodeBase64Image(asset: LottieImageAsset) {
    if (asset.bitmap != null) return
    val filename = asset.fileName
    if (filename.startsWith("data:") && filename.indexOf("base64,") > 0) {
        // Contents look like a base64 data URI, with the format data:image/png;base64,<data>.
        try {
            val data = Base64.decode(filename.substring(filename.indexOf(',') + 1), Base64.DEFAULT)
            val opts = BitmapFactory.Options()
            opts.inScaled = true
            opts.inDensity = 160
            asset.bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, opts)
        } catch (e: IllegalArgumentException) {
            Logger.warning("data URL did not have correct base64 format.", e)
        }
    }
}

private fun String?.ensureTrailingSlash(): String? = when {
    this == null -> null
    endsWith('/') -> this
    else -> "$this/"
}