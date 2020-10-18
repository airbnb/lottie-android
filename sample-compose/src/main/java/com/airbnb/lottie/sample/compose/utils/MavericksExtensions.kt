package com.airbnb.lottie.sample.compose.utils


import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ContextAmbient
import androidx.fragment.app.FragmentActivity
import com.airbnb.mvrx.*
import kotlinx.coroutines.flow.map
import kotlin.reflect.KProperty

@Composable
fun <VM : MavericksViewModel<S>, S : MavericksState> VM.collectState(): S {
    val state by stateFlow.collectAsState(initial = com.airbnb.mvrx.withState(this) { it })
    return state
}

@Composable
fun <VM : MavericksViewModel<S>, S : MavericksState, O : Any?> VM.collectState(mapper: (S) -> O): O {
    val state by stateFlow.map { mapper(it) }.collectAsState(initial = mapper(com.airbnb.mvrx.withState(this) { it }))
    return state
}

@Composable
inline fun <reified VM : MavericksViewModel<S>, reified S : MavericksState> mavericksViewModelAndState(): Pair<VM, S> {
    val viewModelClass = VM::class
    val activity = ContextAmbient.current as? FragmentActivity ?: error("Composable is not hosted in a FragmentActivity")
    val keyFactory = { viewModelClass.java.name }
    val viewModel = MavericksViewModelProvider.get(
        viewModelClass = viewModelClass.java,
        stateClass = S::class.java,
        viewModelContext = ActivityViewModelContext(activity, activity.intent.extras?.get(Mavericks.KEY_ARG)),
        key = keyFactory()
    )
    val state by viewModel.stateFlow.collectAsState(initial = com.airbnb.mvrx.withState(viewModel) { it })
    return viewModel to state
}

@Composable
inline fun <reified VM : MavericksViewModel<S>, reified S : MavericksState> mavericksViewModel(): VM {
    val viewModelClass = VM::class
    val activity = ContextAmbient.current as? FragmentActivity ?: error("Composable is not hosted in a FragmentActivity")
    val keyFactory = { viewModelClass.java.name }
    val viewModel = MavericksViewModelProvider.get(
        viewModelClass = viewModelClass.java,
        stateClass = S::class.java,
        viewModelContext = ActivityViewModelContext(activity, activity.intent.extras?.get(Mavericks.KEY_ARG)),
        key = keyFactory()
    )
    return viewModel
}

/**
 * Permits property delegation of `val`s using `by` for [State].
 *
 * @sample androidx.compose.runtime.samples.DelegatedReadOnlyStateSample
 */
@Suppress("NOTHING_TO_INLINE")
inline operator fun <VM : MavericksViewModel<S>, S : MavericksState> Pair<VM, State<S>>.getValue(thisObj: Any?, property: KProperty<*>): Pair<VM, S> = first to second.value