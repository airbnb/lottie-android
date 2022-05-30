package com.airbnb.lottie.snapshots.tests

import android.graphics.Matrix
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.snapshotComposable
import com.airbnb.lottie.snapshots.withAnimationView

class LargeCompositionSoftwareRendering : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        snapshotWithImageView("Default") {}
        snapshotWithImageView("CenterCrop") { av ->
            av.scaleType = ImageView.ScaleType.CENTER_CROP
        }
        snapshotWithImageView("Center") { av ->
            av.scaleType = ImageView.ScaleType.CENTER
        }
        snapshotWithImageView("CenterInside") { av ->
            av.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        snapshotWithImageView("FitCenter") { av ->
            av.scaleType = ImageView.ScaleType.FIT_CENTER
        }
        snapshotWithImageView("FitStart") { av ->
            av.scaleType = ImageView.ScaleType.FIT_START
        }
        snapshotWithImageView("FitEnd") { av ->
            av.scaleType = ImageView.ScaleType.FIT_END
        }
        snapshotWithImageView("FitXY") { av ->
            av.scaleType = ImageView.ScaleType.FIT_XY
        }
        snapshotWithImageView("Matrix With Sew") { av ->
            av.scaleType = ImageView.ScaleType.MATRIX
            av.imageMatrix = Matrix().apply {
                preScale(0.025f, 0.025f)
                preSkew(1f, 0f)
            }
        }

        snapshotWithComposable("Fit") { comp ->
            LottieAnimation(comp, progress = { 0f }, contentScale = ContentScale.Fit)
        }
        snapshotWithComposable("Crop") { comp ->
            LottieAnimation(comp, progress = { 0f }, contentScale = ContentScale.Crop)
        }
        snapshotWithComposable("FillBounds") { comp ->
            LottieAnimation(comp, progress = { 0f }, contentScale = ContentScale.FillBounds)
        }
        snapshotWithComposable("FillWidth") { comp ->
            LottieAnimation(comp, progress = { 0f }, contentScale = ContentScale.FillWidth)
        }
        snapshotWithComposable("FillHeight") { comp ->
            LottieAnimation(comp, progress = { 0f }, contentScale = ContentScale.FillHeight)
        }
        snapshotWithComposable("Inside") { comp ->
            LottieAnimation(comp, progress = { 0f }, contentScale = ContentScale.Inside)
        }
        snapshotWithComposable("None") { comp ->
            LottieAnimation(comp, progress = { 0f }, contentScale = ContentScale.None)
        }
    }

    private suspend fun SnapshotTestCaseContext.snapshotWithImageView(snapshotVariant: String, callback: (LottieAnimationView) -> Unit) {
        withAnimationView("Tests/LargeComposition.json", "Large Composition Tests", snapshotVariant, widthPx = 275, heightPx = 275) { av ->
            av.setBackgroundColor(0x7f7f7f7f)
            callback(av)
        }
    }

    private suspend fun SnapshotTestCaseContext.snapshotWithComposable(
        snapshotVariant: String,
        callback: @Composable (composition: LottieComposition) -> Unit
    ) {
        val composition = LottieCompositionFactory.fromAssetSync(context, "Tests/LargeComposition.json").value!!
        snapshotComposable("Large Composition Tests - Compose", snapshotVariant) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.Gray)
            ) {
                callback(composition)
            }
        }
    }
}