package com.airbnb.lottie.snapshots.tests

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.airbnb.lottie.snapshots.LocalSnapshotReady
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.getBitmapFromAssets
import com.airbnb.lottie.snapshots.snapshotComposable

class ComposeDynamicPropertiesTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        snapshotComposable("Compose Dynamic Gradient") {
            val composition = LottieCompositionFactory.fromAssetSync(context, "Tests/DynamicGradient.json").value!!
            val dynamicProperties = rememberLottieDynamicProperties(
                rememberLottieDynamicProperty(
                    LottieProperty.GRADIENT_COLOR,
                    arrayOf(Color.YELLOW, Color.GREEN),
                    "Linear",
                    "Rectangle",
                    "Gradient Fill"
                ),
                rememberLottieDynamicProperty(
                    LottieProperty.GRADIENT_COLOR,
                    arrayOf(Color.YELLOW, Color.GREEN),
                    "Radial",
                    "Rectangle",
                    "Gradient Fill"
                )
            )
            LottieAnimation(composition, { 0f }, dynamicProperties = dynamicProperties)
        }

        val heartComposition = LottieCompositionFactory.fromAssetSync(context, "Tests/Heart.json").value!!
        snapshotComposable("Compose Dynamic Image", "Default") {
            val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Tests/Heart.json"))
            val snapshotReady = LocalSnapshotReady.current
            LaunchedEffect(snapshotReady, composition != null) {
                snapshotReady.value = composition != null
            }
            LottieAnimation(composition, { 0f })
        }
        snapshotComposable("Compose Dynamic Image", "Default - Maintain Original Bounds") {
            LottieAnimation(heartComposition, { 0f }, maintainOriginalImageBounds = true)
        }
        snapshotComposable("Compose Dynamic Image", "Smaller") {
            val bitmap = getBitmapFromAssets("Images/Heart-80.png")
            val dynamicProperties = rememberLottieDynamicProperties(
                rememberLottieDynamicProperty(LottieProperty.IMAGE, bitmap, "Heart"),
            )
            LottieAnimation(heartComposition, { 0f }, dynamicProperties = dynamicProperties)
        }
        snapshotComposable("Compose Dynamic Image", "Smaller - Maintain Original Bounds") {
            val bitmap = getBitmapFromAssets("Images/Heart-80.png")
            val dynamicProperties = rememberLottieDynamicProperties(
                rememberLottieDynamicProperty(LottieProperty.IMAGE, bitmap, "Heart"),
            )
            LottieAnimation(heartComposition, { 0f }, dynamicProperties = dynamicProperties, maintainOriginalImageBounds = true)
        }
        snapshotComposable("Compose Dynamic Image", "Larger") {
            val bitmap = getBitmapFromAssets("Images/Heart-1200.png")
            val dynamicProperties = rememberLottieDynamicProperties(
                rememberLottieDynamicProperty(LottieProperty.IMAGE, bitmap, "Heart"),
            )
            LottieAnimation(heartComposition, { 0f }, dynamicProperties = dynamicProperties)
        }
        snapshotComposable("Compose Dynamic Image", "Larger - Maintain Original Bounds") {
            val bitmap = getBitmapFromAssets("Images/Heart-1200.png")
            val dynamicProperties = rememberLottieDynamicProperties(
                rememberLottieDynamicProperty(LottieProperty.IMAGE, bitmap, "Heart"),
            )
            LottieAnimation(heartComposition, { 0f }, dynamicProperties = dynamicProperties, maintainOriginalImageBounds = true)
        }

        snapshotComposable("Compose switch composition") {
            val snapshotReady = LocalSnapshotReady.current
            var state by remember { mutableStateOf(1) }
            val composition by rememberLottieComposition(LottieCompositionSpec.Asset(if (state == 1) "Tests/Dynamic1.json" else "Tests/Dynamic2.json"))
            val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
            val dynamicProperties = rememberLottieDynamicProperties(
                rememberLottieDynamicProperty(LottieProperty.COLOR, 0x0000FF, "**", "Fill 1")
            )
            val ready = state == 2 && composition != null
            LaunchedEffect(ready) {
                snapshotReady.value = ready
            }
            if (composition != null && state == 1) {
                state = 2
            }
            LottieAnimation(
                composition,
                { progress },
                dynamicProperties = dynamicProperties
            )
        }
    }
}
