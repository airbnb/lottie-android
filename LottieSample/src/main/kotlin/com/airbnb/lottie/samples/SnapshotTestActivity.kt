package com.airbnb.lottie.samples

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_snapshot_tests.*

class SnapshotTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snapshot_tests)
    }

    fun recordSnapshot(snapshotName: String, snapshotVariant: String) {
        counterTextView.post {
            statusTextView.text = if (snapshotVariant == "default") snapshotName else "$snapshotName - $snapshotVariant"
            val count = counterTextView.text.toString().toInt()
            counterTextView.text = "${count + 1}"
        }
    }
}