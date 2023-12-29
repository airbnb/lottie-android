package com.airbnb.lottie.snapshots.tests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.snapshots.R
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
                { 1f },
                renderMode = renderMode,
            )
        }

        snapshotComposable("Compose Scale Types", "720p", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                { 1f },
                renderMode = renderMode,
                modifier = Modifier
                    .size(720.dp, 1280.dp),
            )
        }

        snapshotComposable("Compose Scale Types", "300x300@2x", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                { 1f },
                renderMode = renderMode,
                modifier = Modifier
                    .size(300.dp, 300.dp)
                    .scale(2f),
            )
        }

        snapshotComposable("Compose Scale Types", "300x300@4x", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                { 1f },
                renderMode = renderMode,
                modifier = Modifier
                    .size(300.dp, 300.dp)
                    .scale(4f),
            )
        }

        snapshotComposable("Compose Scale Types", "300x300 Crop", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                { 1f },
                contentScale = ContentScale.Crop,
                renderMode = renderMode,
                modifier = Modifier
                    .size(300.dp, 300.dp),
            )
        }

        snapshotComposable("Compose Scale Types", "300x300 Inside", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                { 1f },
                contentScale = ContentScale.Inside,
                renderMode = renderMode,
                modifier = Modifier
                    .size(300.dp, 300.dp),
            )
        }

        snapshotComposable("Compose Scale Types", "300x300 FillBounds", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                { 1f },
                contentScale = ContentScale.FillBounds,
                renderMode = renderMode,
                modifier = Modifier
                    .size(300.dp, 300.dp),
            )
        }

        snapshotComposable("Compose Scale Types", "300x300 Fit 2x", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                { 1f },
                contentScale = ContentScale.Fit,
                renderMode = renderMode,
                modifier = Modifier
                    .size(300.dp, 300.dp)
                    .scale(2f),
            )
        }

        snapshotComposable("Compose Scale Types", "300x300 Crop 2x", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                { 1f },
                contentScale = ContentScale.Crop,
                renderMode = renderMode,
                modifier = Modifier
                    .size(300.dp, 300.dp)
                    .scale(2f),
            )
        }

        snapshotComposable("Compose Scale Types", "600x600 Inside", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                { 1f },
                contentScale = ContentScale.Inside,
                renderMode = renderMode,
                modifier = Modifier
                    .size(600.dp, 600.dp),
            )
        }

        snapshotComposable("Compose Scale Types", "600x600 FillBounds", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                { 1f },
                contentScale = ContentScale.FillBounds,
                renderMode = renderMode,
                modifier = Modifier
                    .size(600.dp, 600.dp),
            )
        }

        snapshotComposable("Compose Scale Types", "600x600 Fit", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                { 1f },
                contentScale = ContentScale.Fit,
                renderMode = renderMode,
                modifier = Modifier
                    .size(600.dp, 600.dp),
            )
        }

        snapshotComposable("Compose Scale Types", "300x600 FitBounds", renderHardwareAndSoftware = true) { renderMode ->
            LottieAnimation(
                composition,
                { 1f },
                contentScale = ContentScale.FillBounds,
                renderMode = renderMode,
                modifier = Modifier
                    .size(300.dp, 600.dp),
            )
        }

        val largeSquareComposition = loadCompositionFromAssetsSync("Tests/LargeSquare.json")
        snapshotComposable("Compose constrained size", "Column", renderHardwareAndSoftware = true) { renderMode ->
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                LottieAnimation(
                    composition = largeSquareComposition,
                    progress = { 1f },
                    contentScale = ContentScale.FillWidth,
                    renderMode = renderMode,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Other content",
                    textAlign = TextAlign.Center,
                )
            }
        }

        snapshotComposable("Compose constrained size", "Row", renderHardwareAndSoftware = true) { renderMode ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .height(128.dp),
            ) {
                val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
                LottieAnimation(
                    composition = largeSquareComposition,
                    progress = { progress },
                    modifier = Modifier.fillMaxHeight(),
                )
                Text("Other content")
            }
        }
    }
}
