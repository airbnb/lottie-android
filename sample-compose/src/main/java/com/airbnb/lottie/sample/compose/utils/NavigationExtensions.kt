package com.airbnb.lottie.sample.compose.utils

import android.os.Bundle
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticAmbientOf
import androidx.navigation.NavController
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

val NavControllerAmbient = staticAmbientOf<NavController> { error("You must specify a NavController.") }

@Composable
fun findNavController() = NavControllerAmbient.current

fun Bundle.getBase64String(key: String) = String(Base64.decode(getString(key), Base64.DEFAULT), StandardCharsets.UTF_8)
