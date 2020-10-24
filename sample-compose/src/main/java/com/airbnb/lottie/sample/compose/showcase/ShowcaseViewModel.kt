package com.airbnb.lottie.sample.compose.showcase

import com.airbnb.lottie.sample.compose.api.AnimationsResponseV2
import com.airbnb.lottie.sample.compose.api.LottieFilesApi
import com.airbnb.lottie.sample.compose.dagger.AssistedViewModelFactory
import com.airbnb.lottie.sample.compose.dagger.DaggerMvRxViewModelFactory
import com.airbnb.mvrx.*
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers

data class ShowcaseState(
    val animations: Async<AnimationsResponseV2> = Uninitialized
) : MavericksState

class ShowcaseViewModel @AssistedInject constructor(
    @Assisted initialState: ShowcaseState,
    private var api: LottieFilesApi
) : MavericksViewModel<ShowcaseState>(initialState) {

    init {
        fetchFeatured()
    }

    private fun fetchFeatured() {
        suspend {
            api.getFeatured()
        }.execute(Dispatchers.IO) {
            copy(animations = it)
        }
    }

    @AssistedInject.Factory
    interface Factory : AssistedViewModelFactory<ShowcaseViewModel, ShowcaseState> {
        override fun create(initialState: ShowcaseState): ShowcaseViewModel
    }

    companion object : DaggerMvRxViewModelFactory<ShowcaseViewModel, ShowcaseState>(ShowcaseViewModel::class.java)
}