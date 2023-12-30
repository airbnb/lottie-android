package com.airbnb.lottie.snapshots.tests

import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.withDrawable

class PolygonStrokeTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        withDrawable("Tests/TriangleLargeStroke.json", "Triangle", "Large Stroke") { drawable ->
            drawable.progress = 0.40999988f
        }
    }
}
