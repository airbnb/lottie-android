package com.airbnb.lottie.snapshots.tests

import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.withDrawable

class FrameBoundariesTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        withDrawable("Tests/Frame.json", "Frame Boundary", "Frame 16 Red") { drawable ->
            drawable.frame = 16
        }
        withDrawable("Tests/Frame.json", "Frame Boundary", "Frame 17 Blue") { drawable ->
            drawable.frame = 17
        }
        withDrawable("Tests/Frame.json", "Frame Boundary", "Frame 50 Blue") { drawable ->
            drawable.frame = 50
        }
        withDrawable("Tests/Frame.json", "Frame Boundary", "Frame 51 Green") { drawable ->
            drawable.frame = 51
        }

        withDrawable("Tests/RGB.json", "Frame Boundary", "Frame 0 Red") { drawable ->
            drawable.frame = 0
        }

        withDrawable("Tests/RGB.json", "Frame Boundary", "Frame 1 Green") { drawable ->
            drawable.frame = 1
        }
        withDrawable("Tests/RGB.json", "Frame Boundary", "Frame 2 Blue") { drawable ->
            drawable.frame = 2
        }

        withDrawable("Tests/2FrameAnimation.json", "Float Progress", "0.0") { drawable ->
            drawable.progress = 0f
        }
    }
}