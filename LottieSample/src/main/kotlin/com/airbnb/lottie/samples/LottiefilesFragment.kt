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
import com.airbnb.lottie.samples.views.MarqueeModel_
import com.airbnb.lottie.samples.views.SectionHeaderViewModel_
import kotlinx.android.synthetic.main.fragment_lottiefiles.*

private val TAG = LottiefilesFragment::class.simpleName
class LottiefilesFragment : Fragment(), EpoxyRecyclerView.ModelBuilderCallback {

    private val lottiefilesService by lazy { (requireContext().applicationContext as LottieApplication).lottiefilesService }
    private val viewModel by lazy { ViewModelProviders.of(this).get(LottiefilesViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.fetchMoreAnimations()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_lottiefiles, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.buildModelsWith(this)
        viewModel.loading.observe(this, Observer { recyclerView.requestModelBuild() })
        viewModel.animationDataList.observe(this, Observer { recyclerView.requestModelBuild() })
    }

    override fun buildModels(controller: EpoxyController) {
        MarqueeModel_()
                .id("lottiefiles")
                .title(R.string.lottiefiles)
                .addTo(controller)

        SectionHeaderViewModel_()
                .id("mode")
                .title(when (viewModel.mode()) {
                    LottiefilesViewModel.Mode.Popular -> R.string.popular
                    LottiefilesViewModel.Mode.Recent -> R.string.recent
                })
                .onClickListener { _ -> viewModel.switchMode() }
                .addIf(viewModel.animationDataList.value != null, controller)

        val lastAnimationData = viewModel.animationDataList.value?.last()
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