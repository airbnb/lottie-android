package com.airbnb.lottie.samples

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.airbnb.lottie.samples.model.AnimationData
import com.airbnb.lottie.samples.model.CompositionArgs
import com.airbnb.lottie.samples.views.AnimationItemViewModel_
import com.airbnb.lottie.samples.views.lottiefilesTabBar
import com.airbnb.lottie.samples.views.marquee
import com.airbnb.lottie.samples.views.searchInputItemView
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_epoxy_recycler_view.*
import kotlin.properties.Delegates

data class LottiefilesState(
        val mode: LottiefilesMode = LottiefilesMode.Recent,
        val query: String = ""
) : MvRxState

class LottiefilesViewModel(initialState: LottiefilesState, private val api: LottiefilesApi) : MvRxViewModel<LottiefilesState>(initialState) {

    private var mode = initialState.mode
    private var query = initialState.query

    val pagedList = LivePagedListBuilder<Int, AnimationData>(object : DataSource.Factory<Int, AnimationData>() {
        override fun create(): DataSource<Int, AnimationData> {
            return LottiefilesDataSource(api, mode, query)
        }
    }, 16).build()

    init {
        selectSubscribe(LottiefilesState::mode, LottiefilesState::query) { mode, query ->
            this.mode = mode
            this.query = query
            pagedList.value?.dataSource?.invalidate()
        }
    }

    fun setMode(mode: LottiefilesMode) = setState { copy(mode = mode) }

    fun setQuery(query: String) = setState { copy(query = query) }

    companion object : MvRxViewModelFactory<LottiefilesViewModel, LottiefilesState> {
        override fun create(viewModelContext: ViewModelContext, state: LottiefilesState): LottiefilesViewModel? {
            val service = viewModelContext.app<LottieApplication>().lottiefilesService
            return LottiefilesViewModel(state, service)
        }
    }
}

class LottiefilesDataSource(
        private val api: LottiefilesApi,
        val mode: LottiefilesMode,
        val query: String
) : PageKeyedDataSource<Int, AnimationData>() {
    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, AnimationData>) {
        if (mode == LottiefilesMode.Search && query.isEmpty()) {
            callback.onResult(emptyList(), null, null)
            return
        }
        when (mode) {
            LottiefilesMode.Popular -> api.getPopular(1)
            LottiefilesMode.Recent -> api.getRecent(1)
            LottiefilesMode.Search -> api.search(query, 1)
        }
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { d -> callback.onResult(d.data, 0, d.total, null, 2.takeIf { d.data.isNotEmpty() }) },
                        { callback.onError(it) }
                )
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, AnimationData>) {
        loadPage(params.key, callback)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, AnimationData>) {
        loadPage(params.key, callback)
    }

    private fun loadPage(page: Int, callback: LoadCallback<Int, AnimationData>) {
        when (mode) {
            LottiefilesMode.Popular -> api.getPopular(page)
            LottiefilesMode.Recent -> api.getRecent(page)
            LottiefilesMode.Search -> api.search(query, page)
        }
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { callback.onResult(it.data, page + 1) },
                        { callback.onError(it) }
                )
    }
}

class LottiefilesFragment : BaseMvRxFragment(R.layout.fragment_epoxy_recycler_view) {
    private val viewModel: LottiefilesViewModel by fragmentViewModel()

    private val controller by lazy {
        object : PagedListEpoxyController<AnimationData>() {

            var mode by Delegates.observable(LottiefilesMode.Recent) { _, _, _ -> requestModelBuild() }

            override fun buildItemModel(currentPosition: Int, item: AnimationData?): EpoxyModel<*> {
                return if (item == null) {
                    AnimationItemViewModel_().id(-currentPosition)
                } else {
                    AnimationItemViewModel_()
                            .id(item.id)
                            .previewUrl(item.preview)
                            .title(item.title)
                            .previewBackgroundColor(item.bgColorInt)
                            .onClickListener { _ ->
                                val intent = PlayerActivity.intent(requireContext(), CompositionArgs(animationData = item))
                                requireContext().startActivity(intent)
                            }

                }
            }

            override fun addModels(models: List<EpoxyModel<*>>) {
                marquee {
                    id("lottiefiles")
                    title(R.string.lottiefiles)
                    subtitle(R.string.lottiefiles_airbnb)
                }

                lottiefilesTabBar {
                    id("mode")
                    mode(mode)
                    recentClickListener { _ ->
                        viewModel.setMode(LottiefilesMode.Recent)
                        requireContext().hideKeyboard()
                    }
                    popularClickListener { _ ->
                        viewModel.setMode(LottiefilesMode.Popular)
                        requireContext().hideKeyboard()
                    }
                    searchClickListener { _ ->
                        viewModel.setMode(LottiefilesMode.Search)
                        requireContext().hideKeyboard()
                    }
                }

                if (mode == LottiefilesMode.Search) {
                    searchInputItemView {
                        id("search")
                        searchClickListener(viewModel::setQuery)
                    }
                }
                super.addModels(models)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.pagedList.observe(this, Observer {
            controller.submitList(it)
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.setController(controller)
    }

    override fun invalidate(): Unit = withState(viewModel) { state ->
        controller.mode = state.mode
    }
}