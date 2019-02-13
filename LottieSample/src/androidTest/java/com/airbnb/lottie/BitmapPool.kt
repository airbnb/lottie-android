package com.airbnb.lottie

import android.content.res.Resources
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal class BitmapPool(resources: Resources) {

    private val screenWidth = resources.displayMetrics.widthPixels
    private val screenHeight = resources.displayMetrics.heightPixels

    private val poolLimit = PoolLimit(MAX_RELEASED_BITMAPS)
    private val bitmaps = Collections.synchronizedList(ArrayList<Bitmap>())
    private val releasedBitmaps = ConcurrentHashMap<Bitmap, Bitmap>()

    fun acquire(width: Int, height: Int): Bitmap {
        Log.d(L.TAG, "Acquiring bitmap of size " + width + "x" + height)

        if (width <= 0 || height <= 0) {
            return TRANSPARENT_1X1_BITMAP
        }

        poolLimit.acquire()

        val fullBitmap = synchronized(bitmaps) {
            bitmaps
                    .firstOrNull { it.width >= width && it.height >= height }
                    ?.also { bitmaps.remove(it) }
        } ?: createNewBitmap(width, height)

        val croppedBitmap = Bitmap.createBitmap(fullBitmap, 0, 0, width, height)
        releasedBitmaps[croppedBitmap] = fullBitmap
        Log.d(L.TAG, "Returning bitmap")
        return croppedBitmap
    }

    fun release(bitmap: Bitmap) {
        Log.d(L.TAG, "Releasing bitmap")

        if (bitmap == TRANSPARENT_1X1_BITMAP) {
            return
        }

        val originalBitmap = releasedBitmaps.remove(bitmap)
                ?: throw IllegalArgumentException("Unable to find original bitmap.")

        bitmaps.add(originalBitmap)

        poolLimit.release()
    }

    private fun createNewBitmap(width: Int, height: Int): Bitmap {
        // Make the bitmap at least as large as the screen so we don't wind up with a fragmented pool of
        // bitmap sizes. We'll crop the right size out of it before returning it in acquire().
        Log.d(L.TAG, "Creating a new bitmap of size " + width + "x" + height)
        return Bitmap.createBitmap(
                Math.max(screenWidth, width),
                Math.max(screenHeight, height),
                Bitmap.Config.ARGB_8888
        )
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


/**
 * Enforces a limit for how many bitmaps can be used at once.
 */
private class PoolLimit(limit: Int) {
    // The actual object sent in the channel is arbitrary and is unused,
    // we just rely on the buffering mechanism to limit how many items can be acquired at once.
    private val bufferedChannel = Channel<Int>(limit)

    /**
     * Returns when the number of current acquired count goes below the limit.
     */
    fun acquire() {
        runBlocking {
            bufferedChannel.send(0)
        }
    }

    /**
     * Must be matched with a call to [acquire] after the item is done being used.
     */
    fun release() {
        runBlocking {
            bufferedChannel.receive()
        }
    }
}