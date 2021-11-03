package com.airbnb.lottie.snapshots.tests

import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.withDrawable

class MarkersTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        withDrawable("Tests/Marker.json", "Marker", "startFrame") { drawable ->
            drawable.setMinAndMaxFrame("Marker A")
            drawable.frame = drawable.minFrame.toInt()
        }

        withDrawable("Tests/Marker.json", "Marker", "endFrame") { drawable ->
            drawable.setMinAndMaxFrame("Marker A")
            drawable.frame = drawable.maxFrame.toInt()
        }

        withDrawable("Tests/RGBMarker.json", "Marker", "->[Green, Blue)") { drawable ->
            drawable.setMinAndMaxFrame("Green Section", "Blue Section", false)
            drawable.frame = drawable.minFrame.toInt()
        }

        withDrawable("Tests/RGBMarker.json", "Marker", "->[Green, Blue]") { drawable ->
            drawable.setMinAndMaxFrame("Green Section", "Blue Section", true)
            drawable.frame = drawable.minFrame.toInt()
        }

        withDrawable("Tests/RGBMarker.json", "Marker", "[Green, Blue)<-") { drawable ->
            drawable.setMinAndMaxFrame("Green Section", "Blue Section", false)
            drawable.frame = drawable.maxFrame.toInt()
        }

        withDrawable("Tests/RGBMarker.json", "Marker", "[Green, Blue]<-") { drawable ->
            drawable.setMinAndMaxFrame("Green Section", "Blue Section", true)
            drawable.frame = drawable.maxFrame.toInt()
        }
    }
}