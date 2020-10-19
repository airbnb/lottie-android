package com.airbnb.lottie.sample.compose.dagger

import com.airbnb.lottie.sample.compose.lottiefiles.LottieFilesRecentAndPopularViewModel
import com.airbnb.lottie.sample.compose.lottiefiles.LottieFilesSearchViewModel
import com.airbnb.lottie.sample.compose.showcase.ShowcaseViewModel
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@AssistedModule
@Module(includes = [AssistedInject_AppModule::class])
interface AppModule {

    @Binds
    @IntoMap
    @ViewModelKey(ShowcaseViewModel::class)
    fun showcaseViewModelFactory(factory: ShowcaseViewModel.Factory): AssistedViewModelFactory<*, *>

    @Binds
    @IntoMap
    @ViewModelKey(LottieFilesSearchViewModel::class)
    fun lottieFilesSearchViewModelFactory(factory: LottieFilesSearchViewModel.Factory): AssistedViewModelFactory<*, *>

    @Binds
    @IntoMap
    @ViewModelKey(LottieFilesRecentAndPopularViewModel::class)
    fun lottieFilesRecentAndPopularViewModelFactory(factory: LottieFilesRecentAndPopularViewModel.Factory): AssistedViewModelFactory<*, *>

}