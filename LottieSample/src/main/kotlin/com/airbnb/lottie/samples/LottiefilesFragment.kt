package com.airbnb.lottie.samples

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.lottie.samples.model.CompositionArgs
import com.airbnb.lottie.samples.views.LoadingViewModel_
import com.airbnb.lottie.samples.views.LottiefilesTabBarModel_
import com.airbnb.lottie.samples.views.MarqueeModel_
import com.airbnb.lottie.samples.views.SearchInputItemViewModel_
import kotlinx.android.synthetic.main.fragment_epoxy_recycler_view.*

private val TAG = LottiefilesFragment::class.simpleName
class LottiefilesFragment : Fragment(), EpoxyRecyclerView.ModelBuilderCallback {

    private val lottiefilesService by lazy { (requireContext().applicationContext as LottieApplication).lottiefilesService }
    private val viewModel by lazy { ViewModelProviders.of(this).get(LottiefilesViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.fetchMoreAnimations()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_epoxy_recycler_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.buildModelsWith(this)
        viewModel.loading.observe(this, Observer { recyclerView.requestModelBuild() })
        viewModel.animationDataList.observe(this, Observer { recyclerView.requestModelBuild() })
    }

    override fun buildModels(controller: EpoxyController) {
        MarqueeModel_()
                .id("lottiefiles")
                .title(R.string.lottiefiles)
                .subtitle(R.string.lottiefiles_airbnb)
                .addTo(controller)

        LottiefilesTabBarModel_()
                .id("mode")
                .mode(viewModel.mode())
                .recentClickListener { _-> viewModel.setMode(LottiefilesMode.Recent) }
                .popularClickListener { _ -> viewModel.setMode(LottiefilesMode.Popular) }
                .searchClickListener { _ -> viewModel.setMode(LottiefilesMode.Search) }
                .addTo(controller)

        SearchInputItemViewModel_()
                .id("search")
                .searchClickListener { viewModel.setMode(LottiefilesMode.Search, it) }
                .addIf(viewModel.mode() == LottiefilesMode.Search, controller)

        val lastAnimationData = viewModel.animationDataList.value?.lastOrNull()
        viewModel.animationDataList.value?.forEach {
            it ?: return@forEach
            val args = CompositionArgs(animationData = it)
            AnimationItemViewModel_()
                    .id(it.id)
                    .animationData(it)
                    .clickListener { _ ->
                        startActivity(PlayerActivity.intent(requireContext(), args))
                    }
                    .onBind({ _, _, _ ->
                        if (it == lastAnimationData) {
                            viewModel.fetchMoreAnimations()
                        }
                    })
                    .addTo(controller)
        }

        LoadingViewModel_()
                .id("loading")
                .onBind { _, _, _ -> viewModel.fetchMoreAnimations() }
                .addIf(viewModel.loading.value ?: false, controller)
    }
}