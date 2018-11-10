package com.airbnb.lottie

import android.graphics.Bitmap
import android.os.Build
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.Deferred

class Snapshot(
        private val transferObserverDeferred: Deferred<TransferObserver>,
        val bitmap: Bitmap,
        val animationName: String,
        val variant: String
) {
    /**
     * You must call await before calling this.
     */
    lateinit var transferObserver: TransferObserver

    private val url get() = "https://s3.amazonaws.com/${transferObserver.bucket}/${transferObserver.key}"

    suspend fun await() {
        transferObserver = transferObserverDeferred.await()
    }

    fun toJson(): JsonElement = JsonObject().apply {
        addProperty("url", url)
        addProperty("variant", animationName)
        addProperty("target", "android${Build.VERSION.SDK_INT}")
        addProperty("component", variant)
        addProperty("width", bitmap.width)
        addProperty("height", bitmap.height)
    }
}