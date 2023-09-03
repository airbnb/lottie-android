package com.airbnb.lottie.samples

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingDataAdapter
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.samples.api.LottiefilesApi
import com.airbnb.lottie.samples.databinding.LottiefilesFragmentBinding
import com.airbnb.lottie.samples.model.AnimationData
import com.airbnb.lottie.samples.model.AnimationResponse
import com.airbnb.lottie.samples.model.CompositionArgs
import com.airbnb.lottie.samples.utils.BaseFragment
import com.airbnb.lottie.samples.utils.hideKeyboard
import com.airbnb.lottie.samples.utils.viewBinding
import com.airbnb.lottie.samples.views.AnimationItemView
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class LottiefilesState(
    val mode: LottiefilesMode = LottiefilesMode.Recent,
    val query: String = ""
) : MavericksState

class LottiefilesViewModel(initialState: LottiefilesState, private val api: LottiefilesApi) : MavericksViewModel<LottiefilesState>(initialState) {

    private var mode = initialState.mode
    private var query = initialState.query

    private var dataSource: LottiefilesDataSource? = null
    val pager = Pager(PagingConfig(pageSize = 16)) {
        LottiefilesDataSource(api, mode, query).also { dataSource = it }
    }.flow.cachedIn(viewModelScope)

    init {
        onEach(LottiefilesState::mode, LottiefilesState::query) { mode, query ->
            this.mode = mode
            this.query = query
            dataSource?.invalidate()
        }
    }

    fun setMode(mode: LottiefilesMode) = setState { copy(mode = mode) }

    fun setQuery(query: String) = setState { copy(query = query) }

    companion object : MavericksViewModelFactory<LottiefilesViewModel, LottiefilesState> {
        override fun create(viewModelContext: ViewModelContext, state: LottiefilesState): LottiefilesViewModel {
            val service = viewModelContext.app<LottieApplication>().lottiefilesService
            return LottiefilesViewModel(state, service)
        }
    }
}

class LottiefilesDataSource(
    private val api: LottiefilesApi,
    val mode: LottiefilesMode,
    private val query: String
) : PagingSource<Int, AnimationData>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AnimationData> {
        val page = params.key ?: 1
        return try {
            val response = when (mode) {
                LottiefilesMode.Popular -> api.getPopular(page)
                LottiefilesMode.Recent -> api.getRecent(page)
                LottiefilesMode.Search -> {
                    if (query.isBlank()) {
                        AnimationResponse(page, emptyList(), "", page, null, "", 0, "", 0, 0)
                    } else {
                        api.search(query, page)
                    }
                }
            }

            LoadResult.Page(
                response.data,
                if (page == 1) null else page + 1,
                (page + 1).takeIf { page < response.lastPage }
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, AnimationData>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

class LottiefilesFragment : BaseFragment(R.layout.lottiefiles_fragment) {
    private val binding: LottiefilesFragmentBinding by viewBinding()
    private val viewModel: LottiefilesViewModel by fragmentViewModel()

    private object AnimationItemDataDiffCallback : DiffUtil.ItemCallback<AnimationData>() {
        override fun areItemsTheSame(oldItem: AnimationData, newItem: AnimationData) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: AnimationData, newItem: AnimationData) = oldItem == newItem
    }

    private class AnimationItemViewHolder(context: Context) : RecyclerView.ViewHolder(AnimationItemView(context)) {
        fun bind(data: AnimationData?) {
            val view = itemView as AnimationItemView
            view.setTitle(data?.title)
            view.setPreviewUrl(data?.preview)
            view.setPreviewBackgroundColor(data?.bgColorInt)
            view.setOnClickListener {
                val intent = PlayerActivity.intent(view.context, CompositionArgs(animationData = data))
                view.context.startActivity(intent)
            }
        }
    }


    private val adapter = object : PagingDataAdapter<AnimationData, AnimationItemViewHolder>(AnimationItemDataDiffCallback) {
        override fun onBindViewHolder(holder: AnimationItemViewHolder, position: Int) = holder.bind(getItem(position))

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = AnimationItemViewHolder(parent.context)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pager.collectLatest(adapter::submitData)
        }
        binding.tabBar.setRecentClickListener {
            viewModel.setMode(LottiefilesMode.Recent)
            requireContext().hideKeyboard()
        }
        binding.tabBar.setPopularClickListener {
            viewModel.setMode(LottiefilesMode.Popular)
            requireContext().hideKeyboard()
        }
        binding.tabBar.setSearchClickListener {
            viewModel.setMode(LottiefilesMode.Search)
            requireContext().hideKeyboard()
        }
        binding.searchView.query.onEach { query ->
            viewModel.setQuery(query)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun invalidate(): Unit = withState(viewModel) { state ->
        binding.searchView.isVisible = state.mode == LottiefilesMode.Search
        binding.tabBar.setMode(state.mode)
    }
}
