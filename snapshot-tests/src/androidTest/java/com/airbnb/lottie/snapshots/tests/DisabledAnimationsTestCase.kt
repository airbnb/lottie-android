package com.airbnb.lottie.snapshots.tests

import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.withDrawable

class DisabledAnimationsTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        withDrawable("Tests/ReducedMotion.json", "System Animations", "Disabled") { drawable ->
            drawable.setSystemAnimationsAreEnabled(false)
            drawable.playAnimation()
        }

        withDrawable("Tests/ReducedMotion.json", "System Animations", "Enabled") { drawable ->
            drawable.setSystemAnimationsAreEnabled(false)
            drawable.playAnimation()
        }
    }
}
