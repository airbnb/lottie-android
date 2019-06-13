package com.airbnb.lottie.samples

import android.os.Build
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class Snapshot(
        private val bucket: String,
        private val key: String,
        private val width: Int,
        private val height: Int,
        private val animationName: String,
        private val variant: String
) {
    private val url get() = "https://s3.amazonaws.com/$bucket/$key"

    fun toJson(): JsonElement = JsonObject().apply {
        addProperty("url", url)
        addProperty("target", "android${Build.VERSION.SDK_INT}")
        addProperty("component", animationName)
        addProperty("variant", variant)
        addProperty("width", width)
        addProperty("height", height)
    }
}