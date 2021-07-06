package com.airbnb.lottie.sample.compose.examples

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.airbnb.lottie.LottieImageAsset
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.sample.compose.R
import com.airbnb.lottie.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ImagesExamplesPage() {
    UsageExamplePageScaffold {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            ExampleCard("Inline Image", "base64 image embedded in json file") {
                InlineImage()
            }
            ExampleCard("Assets Image", "Image stored in assets") {
                ImageAssets()
            }
            ExampleCard("Assets Image Callback", "Load an image manually") {
                ImageAssetCallback()
            }
        }
    }
}

@Composable
fun InlineImage() {
    // Don't cache the composition so the bitmaps can get released once the animation is no longer being used.
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.we_accept_inline_image),
        cacheComposition = false,
    )
    LottieAnimation(
        composition,
        iterations = LottieConstants.IterateForever,
    )
}

@Composable
fun ImageAssets() {
    // Don't cache the composition so the bitmaps can get released once the animation is no longer being used.
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.we_accept),
        cacheComposition = false,
        imageAssetsFolder = "Images/WeAccept",
    )
    LottieAnimation(
        composition,
        iterations = LottieConstants.IterateForever,

    )
}

@Composable
fun ImageAssetCallback() {
    // Don't cache the composition so the bitmaps can get released once the animation is no longer being used.
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.we_accept), cacheComposition = false)
    val imageAsset by derivedStateOf { composition?.images?.get("image_0") }
    val context = LocalContext.current
    LaunchedEffect(imageAsset) {
        withContext(Dispatchers.IO) {
            imageAsset?.bitmap = loadBitmapFromAssets(context, imageAsset)
        }
    }
    LottieAnimation(
        composition,
        iterations = LottieConstants.IterateForever,
    )
}

private fun loadBitmapFromAssets(context: Context, asset: LottieImageAsset?): Bitmap? {
    asset ?: return null
    return try {
        val inputSteam = context.assets.open("Images/WeAccept/${asset.fileName}")
        val opts = BitmapFactory.Options()
        opts.inScaled = true
        opts.inDensity = 1606
        val bitmap = BitmapFactory.decodeStream(inputSteam, null, opts)
        bitmap?.resizeTo(asset.width, asset.height)
    } catch (e: Exception) {
        null
    }
}

private fun Bitmap.resizeTo(width: Int, height: Int): Bitmap? {
    if (width == width && height == height) {
        return this
    }
    val resizedBitmap = Bitmap.createScaledBitmap(this, width, height, true)
    recycle()
    return resizedBitmap
}