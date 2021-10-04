package com.airbnb.lottie.snapshots

interface SnapshotTestCase {
    suspend fun SnapshotTestCaseContext.run()
}