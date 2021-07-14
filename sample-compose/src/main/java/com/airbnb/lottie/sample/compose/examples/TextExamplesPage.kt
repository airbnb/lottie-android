package com.airbnb.lottie.sample.compose.examples

import android.graphics.Typeface
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.airbnb.lottie.sample.compose.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TextExamplesPage() {
    UsageExamplePageScaffold {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            ExampleCard("Default", "Loading fonts using default asset paths") {
                Example1()
            }
            ExampleCard("Font Remapping", "Replace fonts using font remapping") {
                Example2()
            }
            ExampleCard("Dynamic Properties", "Replace fonts with custom typefaces") {
                Example3()
            }
        }
    }
}

@Composable
private fun Example1() {
    // Lottie will automatically look for fonts in src/main/assets/fonts.
    // It will find font files based on the font family specified in the Lottie Json file.
    // You can specify a different assets subfolder by using the fontAssetsFolder parameter.
    // By default, it will look for ttf files.
    // You can specify a different file extension by using the fontFileExtension parameter.
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.name))
    LottieAnimation(
        composition,
        progress = 0f,
    )
}

@Composable
private fun Example2() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.name),
        // Comic Neue is the font family set in the Lottie json file.
        fontRemapping = mapOf("Comic Neue" to "fonts/Roboto.ttf"),
        // Don't cache the composition because it has a custom font remapping.
        cacheComposition = false,
    )
    LottieAnimation(
        composition,
        progress = 0f,
    )
}

@Composable
private fun Example3() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.name))
    val typeface = rememberTypeface("fonts/Roboto.ttf")
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(LottieProperty.TYPEFACE, typeface, "NAME")
    )

    LottieAnimation(
        composition,
        progress = 0f,
        dynamicProperties = dynamicProperties,
    )
}

@Composable
private fun rememberTypeface(path: String): Typeface? {
    var typeface: Typeface? by remember { mutableStateOf(null) }
    val context = LocalContext.current
    LaunchedEffect(path) {
        typeface = null
        withContext(Dispatchers.IO) {
            typeface = Typeface.createFromAsset(context.assets, "fonts/Roboto.ttf")
        }
    }
    return typeface
}