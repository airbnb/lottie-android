package com.airbnb.lottie.sample.compose.dagger

import com.airbnb.lottie.sample.compose.api.ApiModule
import com.airbnb.lottie.sample.compose.showcase.ShowcaseViewModel
import dagger.Component
import javax.inject.Singleton

@Component(
    modules = [
        ApiModule::class,
    ]
)
@Singleton
interface ApplicationComponent {
    fun inject(vm: ShowcaseViewModel)
}