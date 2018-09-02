package com.airbnb.lottie.samples

import android.support.v4.app.FragmentActivity
import com.airbnb.epoxy.EpoxyController
import com.airbnb.lottie.samples.model.AnimationData
import com.airbnb.lottie.samples.model.AnimationResponse
import com.airbnb.lottie.samples.model.CompositionArgs
import com.airbnb.lottie.samples.views.loadingView
import com.airbnb.lottie.samples.views.lottiefilesTabBar
import com.airbnb.lottie.samples.views.marquee
import com.airbnb.lottie.samples.views.searchInputItemView
import com.airbnb.mvrx.*


data class LottiefilesState(
        val mode: LottiefilesMode = LottiefilesMode.Recent,
        val items: List<AnimationData> = emptyList(),
        val request: Async<AnimationResponse> = Uninitialized,
        val query: String = ""
) : MvRxState

class LottiefilesViewModel(
        initialState: LottiefilesState,
        private val service: LottiefilesService) : MvRxViewModel<LottiefilesState>(initialState
) {
    init {
        selectSubscribe(LottiefilesState::mode) { fetchMoreItems() }
    }

    fun fetchMoreItems() = withState { state ->
        if (state.request is Loading) return@withState
        val page = (state.request()?.currentPage ?: -1) + 1
        if (state.request()?.lastPage == page && page > 0) return@withState

        when (state.mode) {
            LottiefilesMode.Recent -> service.getRecent(page)
            LottiefilesMode.Popular -> service.getPopular(page)
            LottiefilesMode.Search -> service.search(state.query)
        }.execute { copy(request = it) }
    }

    fun setMode(mode: LottiefilesMode, query: String = "") = setState {
        if (this.mode == mode && mode != LottiefilesMode.Search) return@setState this
        if (this.mode == mode && mode == LottiefilesMode.Search && this.query == query) return@setState this

        copy(mode = mode, request = Uninitialized, items = emptyList(), query = query)
    }

    companion object : MvRxViewModelFactory<LottiefilesState> {
        @JvmStatic
        override fun create(activity: FragmentActivity, state: LottiefilesState): LottiefilesViewModel {
            val service = (activity.applicationContext as LottieApplication).lottiefilesService
            return LottiefilesViewModel(state, service)
        }

    }
}

class LottiefilesFragment : BaseEpoxyFragment() {
    private val viewModel: LottiefilesViewModel by fragmentViewModel()

    override fun EpoxyController.buildModels() = withState(viewModel) { state ->
        marquee {
            id("lottiefiles")
            title(R.string.lottiefiles)
            subtitle(R.string.lottiefiles_airbnb)
        }

        lottiefilesTabBar {
            id("mode")
            mode(state.mode)
            recentClickListener { _ -> viewModel.setMode(LottiefilesMode.Recent) }
            popularClickListener { _ -> viewModel.setMode(LottiefilesMode.Popular) }
            searchClickListener { _ -> viewModel.setMode(LottiefilesMode.Search) }
        }

        if (state.mode == LottiefilesMode.Search) {
            searchInputItemView {
                id("search")
                searchClickListener { viewModel.setMode(LottiefilesMode.Search, it) }
            }
        }

        state.items.forEach {
            val args = CompositionArgs(animationData = it)
            animationItemView {
                id(it.id)
                animationData(it)
                clickListener { _ ->
                    startActivity(PlayerActivity.intent(requireContext(), args))
                }
                onBind { _, _, _ -> viewModel.fetchMoreItems() }
            }
        }

        if (state.request is Loading) {
            loadingView {
                id("loading")
                onBind { _, _, _ -> viewModel.fetchMoreItems() }
            }
        }
    }
}