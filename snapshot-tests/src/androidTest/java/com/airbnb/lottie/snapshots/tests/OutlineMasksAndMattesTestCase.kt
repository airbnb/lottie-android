package com.airbnb.lottie.snapshots.tests

import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.withFilmStripView

class OutlineMasksAndMattesTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        withFilmStripView(
            "Tests/Masks.json",
            "Outline Masks and Mattes",
            "Enabled"
        ) { filmStripView ->
            filmStripView.setOutlineMasksAndMattes(true)
        }
    }
}