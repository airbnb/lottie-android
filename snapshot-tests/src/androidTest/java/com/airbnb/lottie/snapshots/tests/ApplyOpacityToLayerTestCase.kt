package com.airbnb.lottie.snapshots.tests

import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.withFilmStripView

class ApplyOpacityToLayerTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        withFilmStripView(
            "Tests/OverlapShapeWithOpacity.json",
            "Apply Opacity To Layer",
            "Enabled"
        ) { filmStripView ->
            filmStripView.setApplyingOpacityToLayersEnabled(true)
        }
        withFilmStripView(
            "Tests/OverlapShapeWithOpacity.json",
            "Apply Opacity To Layer",
            "Disabled"
        ) { filmStripView ->
            filmStripView.setApplyingOpacityToLayersEnabled(false)
        }
    }
}