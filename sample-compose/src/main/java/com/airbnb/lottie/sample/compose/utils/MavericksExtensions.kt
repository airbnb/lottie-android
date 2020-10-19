package com.airbnb.lottie.sample.compose.utils


import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ContextAmbient
import androidx.fragment.app.FragmentActivity
import com.airbnb.mvrx.*

@Composable
inline fun <reified VM : MavericksViewModel<S>, reified S : MavericksState> mavericksViewModelAndState(): Pair<VM, S> {
    val viewModelClass = VM::class
    val activity = ContextAmbient.current as? FragmentActivity ?: error("Composable is not hosted in a FragmentActivity")
    val keyFactory = { viewModelClass.java.name }
    @SuppressLint("RestrictedApi")
    val viewModel = MavericksViewModelProvider.get(
        viewModelClass = viewModelClass.java,
        stateClass = S::class.java,
        viewModelContext = ActivityViewModelContext(activity, activity.intent.extras?.get(Mavericks.KEY_ARG)),
        key = keyFactory()
    )
    val state by viewModel.stateFlow.collectAsState(initial = withState(viewModel) { it })
    return viewModel to state
}
