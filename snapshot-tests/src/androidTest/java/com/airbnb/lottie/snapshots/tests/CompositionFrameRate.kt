package com.airbnb.lottie.snapshots.tests

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.snapshotComposable
import com.airbnb.lottie.snapshots.withFilmStripView

class CompositionFrameRate : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        withFilmStripView("Tests/Framerate.json", "Composition frame rate", "View") { filmStripView ->
            filmStripView.setUseCompositionFrameRate(true)
        }

        val composition = LottieCompositionFactory.fromAssetSync(context, "Tests/Framerate.json").value!!
        snapshotComposable("Composition frame rate", "Compose - 0") { renderMode ->
            Column {
                LottieAnimation(
                    composition,
                    { 0f },
                    contentScale = ContentScale.Crop,
                    renderMode = renderMode,
                    modifier = Modifier
                        .size(100.dp, 100.dp)
                )
                LottieAnimation(
                    composition,
                    { 0.15f },
                    contentScale = ContentScale.Crop,
                    renderMode = renderMode,
                    modifier = Modifier
                        .size(100.dp, 100.dp)
                )

                LottieAnimation(
                    composition,
                    { 0.5f },
                    contentScale = ContentScale.Crop,
                    renderMode = renderMode,
                    modifier = Modifier
                        .size(100.dp, 100.dp)
                )

                LottieAnimation(
                    composition,
                    { 0.9f },
                    contentScale = ContentScale.Crop,
                    renderMode = renderMode,
                    modifier = Modifier
                        .size(100.dp, 100.dp)
                )
                LottieAnimation(
                    composition,
                    { 1f },
                    contentScale = ContentScale.Crop,
                    renderMode = renderMode,
                    modifier = Modifier
                        .size(100.dp, 100.dp)
                )
            }
        }
    }
}