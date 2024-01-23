package com.airbnb.lottie.snapshots.tests

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
    }
}
