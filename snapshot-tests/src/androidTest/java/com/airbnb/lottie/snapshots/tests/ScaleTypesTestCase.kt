package com.airbnb.lottie.snapshots.tests

import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.withAnimationView

class ScaleTypesTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        withAnimationView("Lottie Logo 1.json", "Scale Types", "Wrap Content", renderHardwareAndSoftware = true) { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "Match Parent", renderHardwareAndSoftware = true) { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x300@2x", renderHardwareAndSoftware = true) { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scale = 2f
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x300@4x", renderHardwareAndSoftware = true) { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scale = 4f
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x300 centerCrop", renderHardwareAndSoftware = true) { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x300 centerInside", renderHardwareAndSoftware = true) { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x300 fitXY", renderHardwareAndSoftware = true) { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.FIT_XY
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x300 centerInside @2x", renderHardwareAndSoftware = true) { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            animationView.scale = 2f
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x300 centerCrop @2x", renderHardwareAndSoftware = true) { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_CROP
            animationView.scale = 2f
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "600x300 centerInside", renderHardwareAndSoftware = true) { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 600.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "600x300 fitXY", renderHardwareAndSoftware = true) { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 600.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.FIT_XY
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x600 centerInside", renderHardwareAndSoftware = true) { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 600.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x600 fitXY", renderHardwareAndSoftware = true) { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 600.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.FIT_XY
        }
    }

    private val Number.dp get() = this.toFloat() / (Resources.getSystem().displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}