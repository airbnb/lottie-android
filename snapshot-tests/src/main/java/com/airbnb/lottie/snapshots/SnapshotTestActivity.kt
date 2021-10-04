package com.airbnb.lottie.snapshots

import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.snapshots.databinding.SnapshotTestsActivityBinding
import com.airbnb.lottie.snapshots.utils.viewBinding

class SnapshotTestActivity : AppCompatActivity() {
    private val binding: SnapshotTestsActivityBinding by viewBinding()

    fun recordSnapshot(snapshotName: String, snapshotVariant: String) {
        binding.counterTextView.post {
            binding.statusTextView.text = if (snapshotVariant == "default") snapshotName else "$snapshotName - $snapshotVariant"
            val count = binding.counterTextView.text.toString().toInt()
            binding.counterTextView.text = "${count + 1}"
        }
    }
}