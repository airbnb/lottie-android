package com.airbnb.lottie.sample.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticAmbientOf
import androidx.navigation.NavController

val NavControllerAmbient = staticAmbientOf<NavController> { error("You must specify a NavController.") }

@Composable
fun findNavController() = NavControllerAmbient.current
