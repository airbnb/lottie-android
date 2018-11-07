package com.airbnb.lottie.samples

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.ImageAssetDelegate
import com.airbnb.lottie.LottieComposition
import kotlinx.android.synthetic.main.activity_film_strip_snapshot_tests.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FilmStripSnapshotTestActivity : AppCompatActivity() {
    private val dummyBitmap by lazy { BitmapFactory.decodeResource(resources, R.drawable.airbnb); }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_film_strip_snapshot_tests)
        filmStripView.setImageAssetDelegate(ImageAssetDelegate { dummyBitmap })
    }

    suspend fun snapshot(composition: LottieComposition) = suspendCoroutine<Bitmap> { continuation ->
        filmStripView.post {
            val bitmap = Bitmap.createBitmap(filmStripView.width, filmStripView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            filmStripView.setComposition(composition)
            filmStripView.draw(canvas)
            continuation.resume(bitmap)
        }
    }
}