package com.airbnb.lottie.snapshots.tests

import android.graphics.Matrix
import android.widget.ImageView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
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
        snapshotWithImageView("CenterCrop") { av ->
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
    }

    private suspend fun SnapshotTestCaseContext.snapshotWithImageView(snapshotVariant: String, callback: (LottieAnimationView) -> Unit) {
        withAnimationView("Tests/LargeComposition.json", "Large composition software render", snapshotVariant, callback)
    }
}