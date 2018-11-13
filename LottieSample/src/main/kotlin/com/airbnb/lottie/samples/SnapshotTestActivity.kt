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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SnapshotTestActivity : AppCompatActivity() {
    private val dummyBitmap by lazy { BitmapFactory.decodeResource(resources, R.drawable.airbnb); }
    private val bitmapPool = BitmapPool()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snapshot_tests)
        filmStripView.setImageAssetDelegate(ImageAssetDelegate { dummyBitmap })
    }

    fun getAnimationView() = animationView

    suspend fun snapshotAnimationView(): Bitmap {
        animationView.isVisible = true
        filmStripView.isVisible = false
        val bitmap = bitmapPool.acquire(animationView.width.coerceAtLeast(1), animationView.height.coerceAtLeast(1))
        val canvas = Canvas(bitmap)
        animationView.draw(canvas)
        return bitmap
    }

    suspend fun snapshotFilmstrip(composition: LottieComposition) = suspendCoroutine<Bitmap> { continuation ->
        filmStripView.post {
            animationView.isVisible = false
            filmStripView.isVisible = true
            val bitmap = Bitmap.createBitmap(filmStripView.width, filmStripView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            filmStripView.setComposition(composition)
            filmStripView.draw(canvas)
            continuation.resume(bitmap)
        }
    }

    private class BitmapPool() {
        private val bitmaps = mutableListOf<Bitmap>()

        @Synchronized
        fun acquire(width: Int, height: Int): Bitmap {
            return bitmaps.firstOrNull { it.width == width && it.height == height } ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }

        @Synchronized
        fun release(bitmap: Bitmap) {
            bitmaps.add(bitmap)
        }
    }
}