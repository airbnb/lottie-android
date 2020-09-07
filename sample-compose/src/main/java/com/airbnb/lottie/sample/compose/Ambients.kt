package com.airbnb.lottie.sample.compose

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ambientOf
import androidx.compose.runtime.staticAmbientOf
import androidx.compose.ui.platform.ContextAmbient

val BackPressedDispatcherAmbient = ambientOf<OnBackPressedDispatcher> { error("No BackPressedDispatcher specified.") }