package com.airbnb.lottie.snapshots.tests

import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.withDrawable

class PartialFrameProgressTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        withDrawable("Tests/2FrameAnimation.json", "Float Progress", "0") { drawable ->
            drawable.progress = 0f
        }

        withDrawable("Tests/2FrameAnimation.json", "Float Progress", "0.25") { drawable ->
            drawable.progress = 0.25f
        }

        withDrawable("Tests/2FrameAnimation.json", "Float Progress", "0.5") { drawable ->
            drawable.progress = 0.5f
        }

        withDrawable("Tests/2FrameAnimation.json", "Float Progress", "1.0") { drawable ->
            drawable.progress = 1f
        }
    }
}