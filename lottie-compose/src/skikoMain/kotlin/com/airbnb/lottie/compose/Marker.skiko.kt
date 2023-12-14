package com.airbnb.lottie.compose

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
actual class Marker(
    @SerialName("tm")
    actual val startFrame: Float,
    @SerialName("cm")
    internal val name : String,
    @SerialName("dr")
    actual val durationFrames : Float
)
