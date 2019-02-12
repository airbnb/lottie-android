package com.airbnb.lottie

import android.graphics.Bitmap
import android.util.Log

class FixedSizeBitmapPool(private val width: Int, private val height: Int, private val size: Int = 5) {
    private val bitmaps = Array<Bitmap?>(size) { null }

    @Synchronized
    fun acquire(): Bitmap {
        for (i in 0 until size) {
            bitmaps[i]?.let { bitmap ->
                bitmaps[i] = null
                return bitmap
            }
        }
        Log.d(L.TAG, "Creating new bitmap")
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    @Synchronized
    fun release(bitmap: Bitmap) {
        for (i in 0 until size) {
            if (bitmaps[i] == null) {
                bitmaps[i]  = bitmap
                return
            }
        }
        Log.d(L.TAG, "Unable to find space to release bitmap.")
        bitmap.recycle()
    }
}