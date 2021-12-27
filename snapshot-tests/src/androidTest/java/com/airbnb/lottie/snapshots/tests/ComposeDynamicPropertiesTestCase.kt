package com.airbnb.lottie.snapshots.tests

import android.graphics.Color
import androidx.compose.ui.platform.ComposeView
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.snapshotComposable
import com.airbnb.lottie.snapshots.snapshotComposition

class ComposeDynamicPropertiesTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        val composition = LottieCompositionFactory.fromAssetSync(context, "Tests/DynamicGradient.json").value!!
        snapshotComposable("Compose Dynamic Gradient") {
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
    }
}