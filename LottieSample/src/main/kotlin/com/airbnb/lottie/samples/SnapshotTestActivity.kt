package com.airbnb.lottie.samples

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.ImageAssetDelegate
import com.airbnb.lottie.LottieComposition
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import kotlinx.android.synthetic.main.activity_film_strip_snapshots.*
import kotlinx.android.synthetic.main.activity_snapshot_tests.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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