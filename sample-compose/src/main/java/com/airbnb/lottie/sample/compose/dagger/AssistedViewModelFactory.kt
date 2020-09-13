package com.airbnb.lottie.sample.compose.dagger

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MvRxState

interface AssistedViewModelFactory<VM : MavericksViewModel<S>, S : MvRxState> {
    fun create(initialState: S): VM
}