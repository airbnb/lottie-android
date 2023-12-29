package com.airbnb.lottie.snapshots.tests

import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.withFilmStripView

class ClipTextToBoundingBoxTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        withFilmStripView(
            "Tests/SzGlyph.json",
            "Clip glyph text to bounding box",
            "Enabled"
        ) { filmStripView ->
            filmStripView.setClipTextToBoundingBox(true)
        }
        withFilmStripView(
            "Tests/SzGlyph.json",
            "Clip glyph text to bounding box",
            "Disabled"
        ) { filmStripView ->
            filmStripView.setClipTextToBoundingBox(true)
        }

        withFilmStripView(
            "Tests/SzFont.json",
            "Clip font text to bounding box",
            "Enabled"
        ) { filmStripView ->
            filmStripView.setClipTextToBoundingBox(true)
        }
        withFilmStripView(
            "Tests/SzFont.json",
            "Clip font text to bounding box",
            "Disabled"
        ) { filmStripView ->
            filmStripView.setClipTextToBoundingBox(true)
        }
    }
}
