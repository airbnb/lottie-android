package com.airbnb.lottie.sample.compose

import androidx.multidex.MultiDexApplication
import com.airbnb.lottie.sample.compose.dagger.ApplicationComponent
import com.airbnb.lottie.sample.compose.dagger.DaggerApplicationComponent

class LottieComposeApplication : MultiDexApplication() {
    lateinit var component: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        component = DaggerApplicationComponent.create()
    }
}