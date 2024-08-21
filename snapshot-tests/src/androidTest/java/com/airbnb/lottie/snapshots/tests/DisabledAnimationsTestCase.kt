package com.airbnb.lottie.snapshots.tests

import com.airbnb.lottie.Lottie
import com.airbnb.lottie.LottieConfig
import com.airbnb.lottie.configurations.reducemotion.ReducedMotionMode
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.withDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DisabledAnimationsTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        withDrawable("Tests/ReducedMotion.json", "System Animations", "Disabled") { drawable ->
            withContext(Dispatchers.Main) {
                drawable.setSystemAnimationsAreEnabled(false)
                drawable.playAnimation()
            }
        }

        withDrawable("Tests/ReducedMotion.json", "System Animations", "Enabled") { drawable ->
            withContext(Dispatchers.Main) {
                drawable.setSystemAnimationsAreEnabled(false)
                drawable.playAnimation()
            }
        }
        withDrawable("Tests/ReducedMotion.json", "System Animations", "Lottie config Disabled") { drawable ->
            withContext(Dispatchers.Main) {
                disableSystemAnimation()
                drawable.playAnimation()
            }
        }

        withDrawable("Tests/ReducedMotion.json", "System Animations", "Lottie config enabled") { drawable ->
            withContext(Dispatchers.Main) {
                disableSystemAnimation(disable = false)
                drawable.playAnimation()
            }
        }
    }

    private fun disableSystemAnimation(disable: Boolean = true) {
        Lottie.initialize(
            LottieConfig.Builder().setReducedMotionOption {
                if (disable) {
                    ReducedMotionMode.REDUCED_MOTION
                } else {
                    ReducedMotionMode.STANDARD_MOTION
                }
            }.build(),
        )
    }
}
