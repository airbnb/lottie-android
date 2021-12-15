package com.airbnb.lottie.issues.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty

class ComposeIssueReproActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Content()
        }
    }

    @Composable
    fun Content() {
        val colorInt1 = 0x00949F // TEAL
        val colorInt2 = 0xAA48FF // PURPLE
        val composition = rememberLottieComposition(spec = LottieCompositionSpec.Asset("test_animation.json"), imageAssetsFolder = "images")
        val dynamicProperties = rememberLottieDynamicProperties(
            rememberLottieDynamicProperty(
                property = LottieProperty.GRADIENT_COLOR,
                keyPath = arrayOf("5050", "Rectangle 1", "Gradient Fill 1"),
                callback = { arrayOf(colorInt1, linearInterpolateHalf(colorInt1, colorInt2), colorInt2) }
            ),
            rememberLottieDynamicProperty(
                property = LottieProperty.GRADIENT_COLOR,
                keyPath = arrayOf("5051", "Rectangle 1", "Gradient Fill 1"),
                callback = { arrayOf(colorInt1, linearInterpolateHalf(colorInt1, colorInt2), colorInt2) }
            ),
        )
        LottieAnimation(
            composition = composition.value,
            dynamicProperties = dynamicProperties,
            isPlaying = true,
            iterations = LottieConstants.IterateForever,
        )
    }

    private fun linearInterpolateHalf(colorA: Int, colorB: Int): Int {
        val rA = (colorA shr 16) and 0xFF
        val gA = (colorA shr 8) and 0xFF
        val bA = (colorA shr 0) and 0xFF

        val rB = (colorB shr 16) and 0xFF
        val gB = (colorB shr 8) and 0xFF
        val bB = (colorB shr 0) and 0xFF

        val r = (rA + rB) / 2
        val g = (gA + gB) / 2
        val b = (bA + bB) / 2

        return ((0xFF000000) + (r shl 16) + (g shl 8) + b).toInt()
    }
}
