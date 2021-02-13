package com.airbnb.lottie.sample.compose.utils

import android.os.Bundle
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController
import java.nio.charset.StandardCharsets

val LocalNavController = staticCompositionLocalOf<NavController> { error("You must specify a NavController.") }

@Composable
fun findNavController() = LocalNavController.current

fun Bundle.getBase64String(key: String) = String(Base64.decode(getString(key), Base64.DEFAULT), StandardCharsets.UTF_8)
