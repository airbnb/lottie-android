package com.airbnb.lottie

import android.graphics.Bitmap
import android.util.Log
import java.util.*
import java.util.concurrent.Semaphore
import kotlin.collections.HashSet

internal class ObjectPool<T>(private val factory: () -> T) {

    private val semaphore = Semaphore(MAX_RELEASED_OBJECTS)
    private val objects = Collections.synchronizedList(ArrayList<T>())
    private val releasedObjects = HashSet<T>()

    fun acquire(): T {
        semaphore.acquire()

        val obj = synchronized(objects) {
            objects.firstOrNull()?.also { objects.remove(it) }
        } ?: factory()

        releasedObjects += obj
        Log.d(L.TAG, "Returning bitmap")
        return obj
    }

    fun release(obj: T) {
        Log.d(L.TAG, "Releasing object")
        val removed = releasedObjects.remove(obj)
        if (!removed) throw IllegalArgumentException("Unable to find original obj.")

        objects.add(obj)
        semaphore.release()
    }

    companion object {
        // The maximum number of objects that are allowed out at a time.
        // If this limit is reached a thread must wait for another bitmap to be returned.
        // Bitmaps are expensive, and if we aren't careful we can easily allocate too many objects
        // since coroutines run parallelized.
        private const val MAX_RELEASED_OBJECTS = 10
    }
}