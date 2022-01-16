package com.airbnb.lottie.snapshots.tests

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.loadCompositionFromAssetsSync
import com.airbnb.lottie.snapshots.snapshotComposable

class ComposeScaleTypesTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        val composition = loadCompositionFromAssetsSync("Lottie Logo 1.json")
        snapshotComposable("Compose Scale Types", "Wrap Content", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                1f,
                renderMode = renderMode,
            )
        }

        snapshotComposable("Compose Scale Types", "720p", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                1f,
                renderMode = renderMode,
                modifier = Modifier
                    .size(720.dp, 1280.dp)
            )
        }

        snapshotComposable("Compose Scale Types", "300x300@2x", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                1f,
                renderMode = renderMode,
                modifier = Modifier
                    .size(300.dp, 300.dp)
                    .scale(2f)
            )
        }

        snapshotComposable("Compose Scale Types", "300x300@4x", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                1f,
                renderMode = renderMode,
                modifier = Modifier
                    .size(300.dp, 300.dp)
                    .scale(4f)
            )
        }

        snapshotComposable("Compose Scale Types", "300x300 Crop", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                1f,
                contentScale = ContentScale.Crop,
                renderMode = renderMode,
                modifier = Modifier
                    .size(300.dp, 300.dp)
            )
        }

        snapshotComposable("Compose Scale Types", "300x300 Inside", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                1f,
                contentScale = ContentScale.Inside,
                renderMode = renderMode,
                modifier = Modifier
                    .size(300.dp, 300.dp)
            )
        }

        snapshotComposable("Compose Scale Types", "300x300 FillBounds", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                1f,
                contentScale = ContentScale.FillBounds,
                renderMode = renderMode,
                modifier = Modifier
                    .size(300.dp, 300.dp)
            )
        }

        snapshotComposable("Compose Scale Types", "300x300 Fit 2x", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                1f,
                contentScale = ContentScale.Fit,
                renderMode = renderMode,
                modifier = Modifier
                    .size(300.dp, 300.dp)
                    .scale(2f)
            )
        }

        snapshotComposable("Compose Scale Types", "300x300 Crop 2x", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                1f,
                contentScale = ContentScale.Crop,
                renderMode = renderMode,
                modifier = Modifier
                    .size(300.dp, 300.dp)
                    .scale(2f)
            )
        }

        snapshotComposable("Compose Scale Types", "600x600 Inside", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                1f,
                contentScale = ContentScale.Inside,
                renderMode = renderMode,
                modifier = Modifier
                    .size(600.dp, 600.dp)
            )
        }

        snapshotComposable("Compose Scale Types", "600x600 FillBounds", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                1f,
                contentScale = ContentScale.FillBounds,
                renderMode = renderMode,
                modifier = Modifier
                    .size(600.dp, 600.dp)
            )
        }

        snapshotComposable("Compose Scale Types", "600x600 Fit", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                1f,
                contentScale = ContentScale.Fit,
                renderMode = renderMode,
                modifier = Modifier
                    .size(600.dp, 600.dp)
            )
        }

        snapshotComposable("Compose Scale Types", "300x600 FitBounds", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                1f,
                contentScale = ContentScale.FillBounds,
                renderMode = renderMode,
                modifier = Modifier
                    .size(300.dp, 600.dp)
            )
        }
    }
}