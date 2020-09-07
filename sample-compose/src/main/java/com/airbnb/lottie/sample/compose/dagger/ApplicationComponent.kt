package com.airbnb.lottie.sample.compose.dagger

import com.airbnb.lottie.sample.compose.api.ApiModule
import com.airbnb.lottie.sample.compose.showcase.ShowcaseViewModel
import com.airbnb.mvrx.MavericksViewModel
import dagger.Component
import javax.inject.Singleton

@Component(
    modules = [
        ApiModule::class,
        AppModule::class
    ]
)
@Singleton
interface ApplicationComponent {
    fun viewModelFactories(): Map<Class<out MavericksViewModel<*>>, AssistedViewModelFactory<*, *>>
}