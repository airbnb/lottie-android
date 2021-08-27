package com.airbnb.lottie.sample.compose

import android.app.Application
import com.airbnb.lottie.L
import com.airbnb.lottie.sample.compose.dagger.ApplicationComponent
import com.airbnb.lottie.sample.compose.dagger.DaggerApplicationComponent
import com.airbnb.mvrx.Mavericks

class LottieComposeApplication : Application() {
    lateinit var component: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        Mavericks.initialize(this)
        component = DaggerApplicationComponent.create()
        L.DBG = true
        @Suppress("RestrictedApi")
        L.setTraceEnabled(true)
    }
}