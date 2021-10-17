package com.airbnb.lottie.snapshots.tests

import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.withFilmStripView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class WalletTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() =
        repeat(10) { i ->
            coroutineScope {
                repeat(2) { i2 ->
                    launch(Dispatchers.IO) {
                        withFilmStripView("walletnfcrel_thermo_intro.json", "wallet", (i * 4 + i2).toString()) {

                        }
                    }
                }
            }
        }
}