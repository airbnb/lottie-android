package com.airbnb.lottie.sample.compose.lottiefiles

import android.util.Log
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Surface
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.airbnb.lottie.sample.compose.R
import com.airbnb.lottie.sample.compose.api.AnimationDataV2
import com.airbnb.lottie.sample.compose.api.LottieFilesApi
import com.airbnb.lottie.sample.compose.composables.AnimationRow
import com.airbnb.lottie.sample.compose.dagger.AssistedViewModelFactory
import com.airbnb.lottie.sample.compose.dagger.DaggerMvRxViewModelFactory
import com.airbnb.lottie.sample.compose.findNavController
import com.airbnb.lottie.sample.compose.utils.mavericksViewModelAndState
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.asMavericksArgs
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class LottieFilesSearchState(
    val query: String = "Loading",
    val results: List<AnimationDataV2> = emptyList(),
    val currentPage: Int = 1,
    val lastPage: Int = 0,
    val fetchException: Boolean = false,
) : MavericksState

class LottieFilesSearchViewModel @AssistedInject constructor(
    @Assisted initialState: LottieFilesSearchState,
    private val api: LottieFilesApi
) : MavericksViewModel<LottieFilesSearchState>(initialState) {
    private var fetchJob: Job? = null

    init {
        onEach(LottieFilesSearchState::query) { query ->
            fetchJob?.cancel()
            if (query.isBlank()) {
                setState { copy(results = emptyList(), currentPage = 1, lastPage = 1, fetchException = false) }
            } else {
                fetchJob = viewModelScope.launch(Dispatchers.IO) {
                    val results = try {
                        Log.d("Gabe", "Fetching page 0")
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
        fetchJob = viewModelScope.launch(Dispatchers.IO) {
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

    @AssistedInject.Factory
    interface Factory : AssistedViewModelFactory<LottieFilesSearchViewModel, LottieFilesSearchState> {
        override fun create(initialState: LottieFilesSearchState): LottieFilesSearchViewModel
    }

    companion object : DaggerMvRxViewModelFactory<LottieFilesSearchViewModel, LottieFilesSearchState>(LottieFilesSearchViewModel::class.java)
}

@Composable
fun LottieFilesSearchPage() {
    val (viewModel, state) = mavericksViewModelAndState<LottieFilesSearchViewModel, LottieFilesSearchState>()
    val navController = findNavController()
    LottieFilesSearchPage(
        state,
        viewModel::setQuery,
        viewModel::fetchNextPage,
        { navController.navigate(R.id.player, it.asMavericksArgs()) }
    )
}

@Composable
fun LottieFilesSearchPage(
    state: LottieFilesSearchState,
    setQuery: (String) -> Unit,
    fetchNextPage: () -> Unit,
    onAnimationClicked: (AnimationDataV2) -> Unit,
    modifier: Modifier = Modifier
) {
    var readyToFetchNextPage by remember { mutableStateOf(false) }
    onCommit(state.results.size) {
        readyToFetchNextPage = false
    }
    onCommit(readyToFetchNextPage) {
        if (readyToFetchNextPage) fetchNextPage()
    }

    Box {
        Column(
            modifier = Modifier.then(modifier)
        ) {
            TextField(
                value = state.query,
                onValueChange = { query -> setQuery(query) },
                label = { Text("Query") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            )
            LazyColumnFor(
                state.results,
                modifier = Modifier.weight(1f)
            ) { result ->
                val resultIndex = state.results.indexOf(result)
                readyToFetchNextPage = resultIndex >= (state.results.size - 20)
                AnimationRow(
                    title = result.title,
                    previewUrl = result.preview_url ?: "",
                    previewBackgroundColor = result.bgColor,
                    onClick = { onAnimationClicked(result) }
                )
            }
        }
        if (state.fetchException) {
            FloatingActionButton(
                onClick = fetchNextPage,
                icon = {
                    Icon(Icons.Filled.Repeat, tint = Color.White)
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
fun previewSearchPage() {
    val data = AnimationDataV2(0, null, "https://assets9.lottiefiles.com/render/k1821vf5.png", "Loading", "")
    val state = LottieFilesSearchState(
        results = listOf(data, data, data),
        fetchException = true
    )
    Surface(color = Color.White) {
        LottieFilesSearchPage(
            state = state,
            setQuery = {},
            fetchNextPage = {},
            onAnimationClicked = {}
        )
    }
}