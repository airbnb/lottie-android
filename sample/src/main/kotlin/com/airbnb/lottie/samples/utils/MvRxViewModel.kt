package com.airbnb.lottie.samples.utils

import androidx.lifecycle.viewModelScope
import com.airbnb.lottie.samples.BuildConfig
import com.airbnb.mvrx.*
import kotlinx.coroutines.*

abstract class MvRxViewModel<S : MvRxState>(initialState: S) : BaseMvRxViewModel<S>(initialState, BuildConfig.DEBUG) {
    /**
     * This uses [Dispatchers.Main.immediate] by default to mimic [viewModelScope].
     */
    fun <T : Any?> (suspend () -> T).execute(
            dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
            reducer: S.(Async<T>) -> S
    ): Job {
        setState { reducer(Loading()) }
        return viewModelScope.launch(dispatcher) {
            try {
                val result = invoke()
                setState { reducer(Success(result)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setState { reducer(Fail(e)) }
            }
        }
    }
}