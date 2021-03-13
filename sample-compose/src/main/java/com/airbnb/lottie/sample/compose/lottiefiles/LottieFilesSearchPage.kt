package com.airbnb.lottie.sample.compose.lottiefiles

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.airbnb.lottie.sample.compose.R
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

data class LottieFilesSearchState(
    val query: String = "",
    val results: List<AnimationDataV2> = emptyList(),
    val currentPage: Int = 1,
    val lastPage: Int = 0,
    val fetchException: Boolean = false,
) : MavericksState

class LottieFilesSearchViewModel @AssistedInject constructor(
    @Assisted initialState: LottieFilesSearchState,
    private val api: LottieFilesApi,
) : MavericksViewModel<LottieFilesSearchState>(initialState) {
    private var fetchJob: Job? = null

    init {
        onEach(LottieFilesSearchState::query) { query ->
            fetchJob?.cancel()
            if (query.isBlank()) {
                setState { copy(results = emptyList(), currentPage = 1, lastPage = 1, fetchException = false) }
            } else {
                fetchJob = viewModelScope.launch {
                    val results = try {
                        api.search(query, 1)
                    } catch (e: Exception) {
                        setState { copy(fetchException = true) }
                        return@launch
                    }
                    setState {
                        copy(
                            results = results.data.map(::AnimationDataV2),
                            currentPage = results.current_page,
                            lastPage = results.last_page,
                            fetchException = false
                        )
                    }
                }
            }
        }
    }

    fun fetchNextPage() = withState { state ->
        fetchJob?.cancel()
        if (state.currentPage >= state.lastPage) return@withState
        fetchJob = viewModelScope.launch {
            val response = try {
                Log.d("Gabe", "Fetching page ${state.currentPage + 1}")
                api.search(state.query, state.currentPage + 1)
            } catch (e: Exception) {
                setState { copy(fetchException = true) }
                return@launch
            }
            setState {
                copy(
                    results = results + response.data.map(::AnimationDataV2),
                    currentPage = response.current_page,
                    fetchException = false
                )
            }
        }
    }

    fun setQuery(query: String) = setState { copy(query = query, currentPage = 1, results = emptyList()) }

    @AssistedFactory
    interface Factory : AssistedViewModelFactory<LottieFilesSearchViewModel, LottieFilesSearchState> {
        override fun create(initialState: LottieFilesSearchState): LottieFilesSearchViewModel
    }

    companion object : MavericksViewModelFactory<LottieFilesSearchViewModel, LottieFilesSearchState> by daggerMavericksViewModelFactory()
}

@Composable
fun LottieFilesSearchPage(navController: NavController) {
    val viewModel: LottieFilesSearchViewModel = mavericksViewModel()
    val state by viewModel.collectAsState()
    LottieFilesSearchPage(
        state,
        viewModel::setQuery,
        viewModel::fetchNextPage,
        onAnimationClicked = { data ->
            navController.navigate(Route.Player.forUrl(data.file, backgroundColor = data.bg_color))
        }
    )
}

@Composable
fun LottieFilesSearchPage(
    state: LottieFilesSearchState,
    onQueryChanged: (String) -> Unit,
    fetchNextPage: () -> Unit,
    onAnimationClicked: (AnimationDataV2) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box {
        Column(
            modifier = Modifier.then(modifier)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChanged,
                label = { Text(stringResource(R.string.query)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                singleLine = true
            )
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

@Preview
@Composable
fun PreviewSearchPage() {
    val data = AnimationDataV2(0, null, "https://assets9.lottiefiles.com/render/k1821vf5.png", "Loading", "")
    val state = LottieFilesSearchState(
        results = listOf(data, data, data),
        fetchException = true
    )
    Surface(color = Color.White) {
        LottieFilesSearchPage(
            state = state,
            onQueryChanged = {},
            fetchNextPage = {},
            onAnimationClicked = {}
        )
    }
}