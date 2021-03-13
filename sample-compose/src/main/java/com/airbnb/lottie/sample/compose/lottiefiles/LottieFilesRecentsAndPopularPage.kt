package com.airbnb.lottie.sample.compose.lottiefiles

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.airbnb.lottie.sample.compose.Route
import com.airbnb.lottie.sample.compose.api.AnimationDataV2
import com.airbnb.lottie.sample.compose.api.LottieFilesApi
import com.airbnb.lottie.sample.compose.composables.AnimationRow
import com.airbnb.lottie.sample.compose.dagger.AssistedViewModelFactory
import com.airbnb.lottie.sample.compose.dagger.daggerMavericksViewModelFactory
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class LottieFilesMode {
    Recent,
    Popular,
}

data class LottieFilesRecentAndPopularState(
    val mode: LottieFilesMode = LottieFilesMode.Recent,
    val results: List<AnimationDataV2> = emptyList(),
    val currentPage: Int = 1,
    val lastPage: Int = 0,
    val fetchException: Boolean = false,
) : MavericksState

class LottieFilesRecentAndPopularViewModel @AssistedInject constructor(
    @Assisted initialState: LottieFilesRecentAndPopularState,
    private val api: LottieFilesApi,
) : MavericksViewModel<LottieFilesRecentAndPopularState>(initialState) {
    private var fetchJob: Job? = null

    init {
        onEach(LottieFilesRecentAndPopularState::mode) {
            setState { copy(results = emptyList(), currentPage = 0, lastPage = 1, fetchException = false) }
            withState {
                fetchNextPage()
            }
        }
    }

    fun fetchNextPage() = withState { state ->
        fetchJob?.cancel()
        if (state.currentPage >= state.lastPage) return@withState
        fetchJob = viewModelScope.launch {
            val response = try {
                Log.d(TAG, "Fetching page ${state.currentPage + 1}")
                when (state.mode) {
                    LottieFilesMode.Recent -> api.getRecent(state.currentPage + 1)
                    LottieFilesMode.Popular -> api.getPopular(state.currentPage + 1)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to fetch from Lottie Files.", e)
                setState { copy(fetchException = true) }
                return@launch
            }
            setState {
                copy(
                    results = results + response.data.map(::AnimationDataV2),
                    currentPage = response.current_page,
                    lastPage = response.last_page,
                    fetchException = false
                )
            }
        }
    }

    fun setMode(mode: LottieFilesMode) = setState { copy(mode = mode) }

    @AssistedFactory
    interface Factory : AssistedViewModelFactory<LottieFilesRecentAndPopularViewModel, LottieFilesRecentAndPopularState> {
        override fun create(initialState: LottieFilesRecentAndPopularState): LottieFilesRecentAndPopularViewModel
    }

    companion object :
        MavericksViewModelFactory<LottieFilesRecentAndPopularViewModel, LottieFilesRecentAndPopularState> by daggerMavericksViewModelFactory() {
        private const val TAG = "LottieFilesVM"
    }
}

@Composable
fun LottieFilesRecentAndPopularPage(navController: NavController, mode: LottieFilesMode) {
    val viewModel: LottieFilesRecentAndPopularViewModel = mavericksViewModel()
    val state by viewModel.collectAsState()
    SideEffect {
        viewModel.setMode(mode)
    }
    LottieFilesRecentAndPopularPage(
        state,
        viewModel::fetchNextPage,
        onAnimationClicked = { data ->
            navController.navigate(Route.Player.forUrl(data.file, backgroundColor = data.bg_color))
        }
    )
}

@Composable
fun LottieFilesRecentAndPopularPage(
    state: LottieFilesRecentAndPopularState,
    fetchNextPage: () -> Unit,
    onAnimationClicked: (AnimationDataV2) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.then(modifier)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(state.results) { index, result ->
                    if (index == state.results.size - 1) {
                        SideEffect(fetchNextPage)
                    }
                    AnimationRow(
                        title = result.title,
                        previewUrl = result.preview_url ?: "",
                        previewBackgroundColor = result.bgColor,
                        onClick = { onAnimationClicked(result) }
                    )
                }
            }
        }
        if (state.fetchException) {
            FloatingActionButton(
                onClick = fetchNextPage,
                content = {
                    Icon(
                        imageVector = Icons.Filled.Repeat,
                        tint = Color.White,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }
}