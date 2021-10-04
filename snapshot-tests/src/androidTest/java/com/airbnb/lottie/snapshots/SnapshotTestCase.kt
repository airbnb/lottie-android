package com.airbnb.lottie.snapshots

/**
 * Implement this and add it to the list of tests cases in [LottieSnapshotTest] to create a new set of snapshot tests.
 * Refer to existing snapshot tests as a reference.
 */
interface SnapshotTestCase {
    suspend fun SnapshotTestCaseContext.run()
}