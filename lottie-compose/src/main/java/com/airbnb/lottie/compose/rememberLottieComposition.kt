package com.airbnb.lottie.compose

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Typeface
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
import com.airbnb.lottie.LottieResult
import com.airbnb.lottie.model.Font
import com.airbnb.lottie.utils.Logger
import com.airbnb.lottie.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.IOException
import java.util.zip.ZipInputStream

/**
 * Use this with [rememberLottieComposition#cacheKey]'s cacheKey parameter to generate a default
 * cache key for the composition.
 */
private const val DefaultCacheKey = "__LottieInternalDefaultCacheKey__"

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
 * @param fontAssetsFolder The default folder Lottie will look in to find font files. Fonts will be matched
 *                         based on the family name specified in the Lottie json file.
 *                         Defaults to "fonts/" so if "Helvetica" was in the Json file, Lottie will auto-match
 *                         fonts located at "src/main/assets/fonts/Helvetica.ttf". Missing fonts will be skipped
 *                         and should be set via fontRemapping or via dynamic properties.
 * @param fontFileExtension The default file extension for font files specified in the fontAssetsFolder or fontRemapping.
 *                          Defaults to ttf.
 * @param cacheKey Set a cache key for this composition. When set, subsequent calls to fetch this composition will
 *                 return directly from the cache instead of having to reload and parse the animation. Set this to
 *                 null to skip the cache. By default, this will automatically generate a cache key derived
 *                 from your [LottieCompositionSpec].
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
    fontAssetsFolder: String = "fonts/",
    fontFileExtension: String = ".ttf",
    cacheKey: String? = DefaultCacheKey,
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
                    fontAssetsFolder.ensureTrailingSlash(),
                    fontFileExtension.ensureLeadingPeriod(),
                    cacheKey,
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
    fontAssetsFolder: String?,
    fontFileExtension: String,
    cacheKey: String?,
): LottieComposition {
    val result = parseCompositionSync(context, spec, cacheKey)
    result.exception?.let { throw it }

    val composition = result.value!!
    loadImagesFromAssets(context, composition, imageAssetsFolder)
    loadFontsFromAssets(context, composition, fontAssetsFolder, fontFileExtension)
    return composition
}

private fun parseCompositionSync(
    context: Context,
    spec: LottieCompositionSpec,
    cacheKey: String?,
): LottieResult<LottieComposition> {
    return when (spec) {
        is LottieCompositionSpec.RawRes -> {
            if (cacheKey == DefaultCacheKey) {
                LottieCompositionFactory.fromRawResSync(context, spec.resId)
            } else {
                LottieCompositionFactory.fromRawResSync(context, spec.resId, cacheKey)
            }
        }
        is LottieCompositionSpec.Url -> {
            if (cacheKey == DefaultCacheKey) {
                LottieCompositionFactory.fromUrlSync(context, spec.url)
            } else {
                LottieCompositionFactory.fromUrlSync(context, spec.url, cacheKey)
            }
        }
        is LottieCompositionSpec.File -> {
            val fis = FileInputStream(spec.fileName)
            when {
                spec.fileName.endsWith("zip") -> LottieCompositionFactory.fromZipStreamSync(
                    ZipInputStream(fis),
                    spec.fileName.takeIf { cacheKey != null },
                )
                else -> LottieCompositionFactory.fromJsonInputStreamSync(fis, spec.fileName.takeIf { cacheKey != null })
            }
        }
        is LottieCompositionSpec.Asset -> {
            if (cacheKey == DefaultCacheKey) {
                LottieCompositionFactory.fromAssetSync(context, spec.assetName)
            } else {
                LottieCompositionFactory.fromAssetSync(context, spec.assetName, null)
            }
        }
        is LottieCompositionSpec.JsonString -> {
            val jsonStringCacheKey = if (cacheKey == DefaultCacheKey) spec.jsonString.hashCode().toString() else cacheKey
            LottieCompositionFactory.fromJsonStringSync(spec.jsonString, jsonStringCacheKey)
        }
        is LottieCompositionSpec.Custom -> {
            try {
                LottieResult(spec.factory())
            } catch (e: Throwable) {
                LottieResult<LottieComposition>(e)
            }
        }
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

private suspend fun loadFontsFromAssets(
    context: Context,
    composition: LottieComposition,
    fontAssetsFolder: String?,
    fontFileExtension: String,
) {
    if (composition.fonts.isEmpty()) return
    withContext(Dispatchers.IO) {
        for (font in composition.fonts.values) {
            maybeLoadTypefaceFromAssets(context, font, fontAssetsFolder, fontFileExtension)
        }
    }
}

private fun maybeLoadTypefaceFromAssets(
    context: Context,
    font: Font,
    fontAssetsFolder: String?,
    fontFileExtension: String,
) {
    val path = "$fontAssetsFolder${font.family}${fontFileExtension}"
    val typefaceWithDefaultStyle = try {
        Typeface.createFromAsset(context.assets, path)
    } catch (e: Exception) {
        Logger.error("Failed to find typeface in assets with path $path.", e)
        return
    }
    try {
        val typefaceWithStyle = typefaceForStyle(typefaceWithDefaultStyle, font.style)
        font.typeface = typefaceWithStyle
    } catch (e: Exception) {
        Logger.error("Failed to create ${font.family} typeface with style=${font.style}!", e)
    }
}

private fun typefaceForStyle(typeface: Typeface, style: String): Typeface? {
    val containsItalic = style.contains("Italic")
    val containsBold = style.contains("Bold")
    val styleInt = when {
        containsItalic && containsBold -> Typeface.BOLD_ITALIC
        containsItalic -> Typeface.ITALIC
        containsBold -> Typeface.BOLD
        else -> Typeface.NORMAL
    }
    return if (typeface.style == styleInt) typeface else Typeface.create(typeface, styleInt)
}

private fun String?.ensureTrailingSlash(): String? = when {
    isNullOrBlank() -> null
    endsWith('/') -> this
    else -> "$this/"
}

private fun String.ensureLeadingPeriod(): String = when {
    isBlank() -> this
    startsWith(".") -> this
    else -> ".$this"
}