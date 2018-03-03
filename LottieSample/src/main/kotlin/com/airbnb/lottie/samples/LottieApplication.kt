package com.airbnb.lottie.samples

import android.app.Application
import okhttp3.OkHttpClient

class LottieApplication : Application() {
    val okHttpClient by lazy { OkHttpClient.Builder().build() }
}