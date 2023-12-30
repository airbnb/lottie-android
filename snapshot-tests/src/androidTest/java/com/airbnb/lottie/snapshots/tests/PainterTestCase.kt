package com.airbnb.lottie.snapshots.tests

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.compose.rememberLottiePainter
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.snapshotComposable

class PainterTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        snapshotComposable("Compose Dynamic Gradient", "0%") {
            val composition = LottieCompositionFactory.fromAssetSync(context, "Tests/Laugh4.json").value!!
            val painter = rememberLottiePainter(composition, 0f)
            Image(
                painter = painter,
                contentDescription = "",
                contentScale = ContentScale.Fit,
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .size(500.dp, 700.dp),
            )
        }

        snapshotComposable("Compose Dynamic Gradient", "50%") {
            val composition = LottieCompositionFactory.fromAssetSync(context, "Tests/Laugh4.json").value!!
            val painter = rememberLottiePainter(composition, 0.5f)
            Image(
                painter = painter,
                contentDescription = "",
                contentScale = ContentScale.Fit,
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .size(500.dp, 700.dp),
            )
        }

        snapshotComposable("Compose Dynamic Gradient", "100%") {
            val composition = LottieCompositionFactory.fromAssetSync(context, "Tests/Laugh4.json").value!!
            val painter = rememberLottiePainter(composition, 1f)
            Image(
                painter = painter,
                contentDescription = "",
                contentScale = ContentScale.Fit,
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .size(500.dp, 700.dp),
            )
        }
    }
}
