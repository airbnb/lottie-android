package com.airbnb.lottie.sample.compose.dagger

import androidx.fragment.app.FragmentActivity
import com.airbnb.lottie.sample.compose.LottieComposeApplication
import com.airbnb.mvrx.*

abstract class DaggerMvRxViewModelFactory<VM : MavericksViewModel<S>, S : MavericksState>(
        private val viewModelClass: Class<out MavericksViewModel<S>>
) : MavericksViewModelFactory<VM, S> {

    override fun create(viewModelContext: ViewModelContext, state: S): VM? {
        return createViewModel(viewModelContext.activity, state)
    }

    private fun <VM : MavericksViewModel<S>, S : MavericksState> createViewModel(
            fragmentActivity:
            FragmentActivity,
            initialState: S
    ): VM {
        val viewModelFactoryMap = (fragmentActivity.application as LottieComposeApplication).component.viewModelFactories()
        val viewModelFactory = viewModelFactoryMap[viewModelClass]

        @Suppress("UNCHECKED_CAST")
        val castedViewModelFactory = viewModelFactory as? AssistedViewModelFactory<VM, S>
        val viewModel = castedViewModelFactory?.create(initialState)
        return viewModel as VM
    }
}