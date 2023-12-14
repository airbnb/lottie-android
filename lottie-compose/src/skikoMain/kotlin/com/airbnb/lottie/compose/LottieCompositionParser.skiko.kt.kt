package com.airbnb.lottie.compose

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json

@Serializable
internal class LottieData(
    private val markers : List<Marker>? = null
) {
    @Transient
    val markersMap : Map<String, Marker>? = markers?.associateBy { it.name }
}

private val json = Json {
    ignoreUnknownKeys = true
}

internal object LottieCompositionParser {
    fun parse(jsonString: String): LottieData =
        json.decodeFromString(jsonString)
}
