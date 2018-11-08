package com.airbnb.lottie.samples

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.airbnb.lottie.ImageAssetDelegate
import com.airbnb.lottie.LottieComposition
import kotlinx.android.synthetic.main.activity_snapshot_tests.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SnapshotTestActivity : AppCompatActivity() {
    private val dummyBitmap by lazy { BitmapFactory.decodeResource(resources, R.drawable.airbnb); }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snapshot_tests)
        filmStripView.setImageAssetDelegate(ImageAssetDelegate { dummyBitmap })
    }

    fun getAnimationView() = animationView

    suspend fun snapshotAnimationView() = suspendCoroutine<Bitmap> { continuation ->
        animationView.isVisible = true
        filmStripView.isVisible = false
        animationView.post {
            val bitmap = Bitmap.createBitmap(animationView.width, animationView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            animationView.draw(canvas)
            continuation.resume(bitmap)
        }
    }

    suspend fun snapshotFilmstrip(composition: LottieComposition) = suspendCoroutine<Bitmap> { continuation ->
        animationView.isVisible = false
        filmStripView.isVisible = true
        filmStripView.post {
            val bitmap = Bitmap.createBitmap(filmStripView.width, filmStripView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            filmStripView.setComposition(composition)
            filmStripView.draw(canvas)
            continuation.resume(bitmap)
        }
    }
}