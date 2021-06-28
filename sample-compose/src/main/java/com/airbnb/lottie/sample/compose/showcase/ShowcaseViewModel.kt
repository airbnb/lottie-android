package com.airbnb.lottie.sample.compose.showcase

import com.airbnb.lottie.sample.compose.api.AnimationsResponseV2
import com.airbnb.lottie.sample.compose.api.LottieFilesApi
import com.airbnb.lottie.sample.compose.dagger.AssistedViewModelFactory
import com.airbnb.lottie.sample.compose.dagger.daggerMavericksViewModelFactory
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Uninitialized
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

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
        }.execute { copy(animations = it) }
    }

    @AssistedFactory
    interface Factory : AssistedViewModelFactory<ShowcaseViewModel, ShowcaseState> {
        override fun create(initialState: ShowcaseState): ShowcaseViewModel
    }

    companion object : MavericksViewModelFactory<ShowcaseViewModel, ShowcaseState> by daggerMavericksViewModelFactory()
}