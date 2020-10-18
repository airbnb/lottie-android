package com.airbnb.lottie.sample.compose

import androidx.activity.OnBackPressedDispatcher
import androidx.compose.runtime.ambientOf

val BackPressedDispatcherAmbient = ambientOf<OnBackPressedDispatcher> { error("No BackPressedDispatcher specified.") }