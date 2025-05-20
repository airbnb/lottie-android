package com.airbnb.lottie.snapshots.tests

import androidx.compose.runtime.getValue
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.getBitmapFromAssets
import com.airbnb.lottie.snapshots.snapshotComposable

class ImageWithShadowsTestCase: SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        snapshotComposable(
            name = "Dynamically injected image with shadow"
        ) {
            val composition = LottieCompositionFactory.fromAssetSync(context, "Tests/ImageWithShadow.json").value!!

            val bitmap = getBitmapFromAssets("Images/airbnb_card.webp")

            val dynamicProperties = rememberLottieDynamicProperties(
                rememberLottieDynamicProperty(LottieProperty.IMAGE, bitmap, "**", "Card.png"),
            )

            LottieAnimation(
                composition = composition,
                progress = { 0f },
                dynamicProperties = dynamicProperties,
            )
        }
    }
}
