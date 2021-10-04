package com.airbnb.lottie.snapshots

import android.content.Context
import com.airbnb.lottie.snapshots.utils.BitmapPool
import com.airbnb.lottie.snapshots.utils.HappoSnapshotter

interface SnapshotTestCaseContext {
    val context: Context
    val snapshotter: HappoSnapshotter
    val bitmapPool: BitmapPool
}