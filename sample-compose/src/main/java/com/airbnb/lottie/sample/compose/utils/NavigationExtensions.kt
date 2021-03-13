package com.airbnb.lottie.sample.compose.utils

import android.os.Bundle
import android.util.Base64
import java.nio.charset.StandardCharsets

fun Bundle.getBase64String(key: String) = String(Base64.decode(getString(key), Base64.DEFAULT), StandardCharsets.UTF_8)
