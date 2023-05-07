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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.airbnb.lottie.sample.compose.R
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
            ExampleCard("Dynamic Properties", "Replace an image with dynamic properties") {
                DynamicProperties()
            }
            ExampleCard("Store on LottieImageAsset", "Store the bitmap within LottieImageAsset") {
                StoredOnImageAsset()
            }
        }
    }
}

@Composable
fun InlineImage() {
    // Don't cache the composition so the bitmaps can get released once the animation is no longer being used.
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.we_accept_inline_image),
        cacheKey = null,
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
        cacheKey = null,
        imageAssetsFolder = "Images/WeAccept",
    )
    LottieAnimation(
        composition,
        iterations = LottieConstants.IterateForever,

    )
}

@Composable
fun DynamicProperties() {
    // Don't cache the composition so the bitmaps can get released once the animation is no longer being used.
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.we_accept),
        cacheKey = null,
    )
    val bitmap = rememberBitmapFromAssets("Images/android.png")

    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(LottieProperty.IMAGE, bitmap, "weaccept.jpg")
    )

    LottieAnimation(
        composition,
        iterations = LottieConstants.IterateForever,
        dynamicProperties = dynamicProperties,
    )
}

@Composable
fun StoredOnImageAsset() {
    // Don't cache the composition so the bitmaps can get released once the animation is no longer being used.
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.we_accept),
        cacheKey = null,
    )
    val imageAsset by remember { derivedStateOf { composition?.images?.get("image_0") } }
    val bitmap = rememberBitmapFromAssets("Images/android.png")
    LaunchedEffect(imageAsset, bitmap) {
        if (imageAsset != null && bitmap != null) {
            // this stores the bitmap on the original composition's image asset which means that it
            // will affect *all* LottieAnimation composables that are rendering this LottieComposition.
            // Use with caution.
            imageAsset?.bitmap = bitmap
        }
    }
    LottieAnimation(
        composition,
        iterations = LottieConstants.IterateForever,
    )
}

@Composable
private fun rememberBitmapFromAssets(asset: String): Bitmap? {
    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    val context = LocalContext.current
    LaunchedEffect(asset) {
        withContext(Dispatchers.IO) {
            bitmap = loadBitmapFromAssets(context, asset)
        }
    }
    return bitmap
}

private fun loadBitmapFromAssets(context: Context, asset: String?): Bitmap? {
    asset ?: return null
    return try {
        val inputSteam = context.assets.open(asset)
        val opts = BitmapFactory.Options()
        opts.inScaled = true
        opts.inDensity = 1606
        BitmapFactory.decodeStream(inputSteam, null, opts)
    } catch (e: Exception) {
        null
    }
}