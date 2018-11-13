package com.airbnb.lottie

import android.graphics.Bitmap
import android.os.Build
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.Deferred

class Snapshot(
        private val transferObserverDeferred: Deferred<*>,
        private val bucket: String,
        private val key: String,
        private val width: Int,
        private val height: Int,
        private val animationName: String,
        private val variant: String
) {
    private val url get() = "https://s3.amazonaws.com/$bucket/$key"

    suspend fun await() {
        transferObserverDeferred.await()
    }

    fun toJson(): JsonElement = JsonObject().apply {
        addProperty("url", url)
        addProperty("target", "android${Build.VERSION.SDK_INT}")
        addProperty("component", animationName)
        addProperty("variant", variant)
        addProperty("width", width)
        addProperty("height", height)
    }
}