package com.airbnb.lottie.snapshots.tests

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.runtime.getValue
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.airbnb.lottie.snapshots.LocalSnapshotReady
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
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
            LottieAnimation(composition, 0f, dynamicProperties = dynamicProperties)
        }

        val heartComposition = LottieCompositionFactory.fromAssetSync(context, "Tests/Heart.json").value!!
        snapshotComposable("Compose Dynamic Image", "Default") {
            val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Tests/Heart.json"))
            LocalSnapshotReady.current.value = composition != null
            LottieAnimation(composition, 0f)
        }
        snapshotComposable("Compose Dynamic Image", "Default - Maintain Original Bounds") {
            LottieAnimation(heartComposition, 0f, maintainOriginalImageBounds = true)
        }
        snapshotComposable("Compose Dynamic Image", "Smaller") {
            val bitmap = getBitmapFromAssets("Images/Heart-80.png")
            val dynamicProperties = rememberLottieDynamicProperties(
                rememberLottieDynamicProperty(LottieProperty.IMAGE, bitmap, "Heart"),
            )
            LottieAnimation(heartComposition, 0f, dynamicProperties = dynamicProperties)
        }
        snapshotComposable("Compose Dynamic Image", "Smaller - Maintain Original Bounds") {
            val bitmap = getBitmapFromAssets("Images/Heart-80.png")
            val dynamicProperties = rememberLottieDynamicProperties(
                rememberLottieDynamicProperty(LottieProperty.IMAGE, bitmap, "Heart"),
            )
            LottieAnimation(heartComposition, 0f, dynamicProperties = dynamicProperties, maintainOriginalImageBounds = true)
        }
        snapshotComposable("Compose Dynamic Image", "Larger") {
            val bitmap = getBitmapFromAssets("Images/Heart-1200.png")
            val dynamicProperties = rememberLottieDynamicProperties(
                rememberLottieDynamicProperty(LottieProperty.IMAGE, bitmap, "Heart"),
            )
            LottieAnimation(heartComposition, 0f, dynamicProperties = dynamicProperties)
        }
        snapshotComposable("Compose Dynamic Image", "Larger - Maintain Original Bounds") {
            val bitmap = getBitmapFromAssets("Images/Heart-1200.png")
            val dynamicProperties = rememberLottieDynamicProperties(
                rememberLottieDynamicProperty(LottieProperty.IMAGE, bitmap, "Heart"),
            )
            LottieAnimation(heartComposition, 0f, dynamicProperties = dynamicProperties, maintainOriginalImageBounds = true)
        }
    }

    private fun SnapshotTestCaseContext.getBitmapFromAssets(name: String): Bitmap {
        @Suppress("BlockingMethodInNonBlockingContext")
        return BitmapFactory.decodeStream(context.assets.open(name), null, BitmapFactory.Options())!!
    }
}