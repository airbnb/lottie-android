package com.airbnb.lottie

import android.graphics.Bitmap
import android.util.Log
import java.util.*
import java.util.concurrent.Semaphore

internal class BitmapPool {
    private val semaphore = Semaphore(MAX_RELEASED_BITMAPS)
    private val bitmaps = Collections.synchronizedList(ArrayList<Bitmap>())
    private val releasedBitmaps = Collections.synchronizedList(ArrayList<Bitmap>())

    fun acquire(width: Int, height: Int): Bitmap {
        Log.d(L.TAG, "Acquiring bitmap of size " + width + "x" + height)

        if (width <= 0 || height <= 0) {
            return TRANSPARENT_1X1_BITMAP
        }

        semaphore.acquire()

        val bitmap = synchronized(bitmaps) {
            bitmaps
                    .firstOrNull { it.width >= width && it.height >= height }
                    ?.also { bitmaps.remove(it) }
        } ?: createNewBitmap(width, height)

        releasedBitmaps += bitmap
        Log.d(L.TAG, "Returning bitmap")
        return bitmap
    }

    fun release(bitmap: Bitmap) {
        Log.d(L.TAG, "Releasing bitmap")

        if (bitmap == TRANSPARENT_1X1_BITMAP) {
            return
        }

        if (!releasedBitmaps.remove(bitmap)) throw IllegalArgumentException("Unable to find original bitmap.")

        bitmaps += bitmap
        semaphore.release()
    }

    private fun createNewBitmap(width: Int, height: Int): Bitmap {
        // Make the bitmap at least as large as the screen so we don't wind up with a fragmented pool of
        // bitmap sizes. We'll crop the right size out of it before returning it in acquire().
        Log.d(L.TAG, "Creating a new bitmap of size " + width + "x" + height)
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    companion object {
        // The maximum number of bitmaps that are allowed out at a time.
        // If this limit is reached a thread must wait for another bitmap to be returned.
        // Bitmaps are expensive, and if we aren't careful we can easily allocate too many bitmaps
        // since coroutines run parallelized.
        private const val MAX_RELEASED_BITMAPS = 10

        private val TRANSPARENT_1X1_BITMAP: Bitmap by lazy {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8)
        }
    }
}