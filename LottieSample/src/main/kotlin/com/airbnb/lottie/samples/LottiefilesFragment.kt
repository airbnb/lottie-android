package com.airbnb.lottie.samples

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
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
        viewModel.fetchAnimations()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_lottiefiles, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.buildModelsWith(this)
        viewModel.loading.observe(this, Observer { recyclerView.requestModelBuild() })
        viewModel.animationResponse.observe(this, Observer { recyclerView.requestModelBuild() })
        viewModel.animationResponseError.observe(this, Observer { recyclerView.requestModelBuild() })
    }

    override fun buildModels(controller: EpoxyController) {
        MarqueeModel_()
                .id("lottiefiles")
                .title(R.string.lottiefiles)
                .addTo(controller)
        SectionHeaderViewModel_()
                .id("popular")
                .title(R.string.popular)
                .addIf(viewModel.animationResponse.value != null, controller)

        LoadingViewModel_()
                .id("loading")
                .addIf(viewModel.loading.value ?: false, controller)

        viewModel.animationResponse.value?.data?.forEach {
            val args = CompositionArgs(animationData = it)
            CompositionCache.fetch(requireContext(), args)
            val cacheLiveData = CompositionCache.cache.getValue(args)
            val compositionResult = cacheLiveData.value
            if (compositionResult is LoadError) {
                Log.e(TAG, "Error getting composition", compositionResult.throwable)
            }
            AnimationItemViewModel_()
                    .id(it.id)
                    .animationData(it)
                    .clickListener { _ ->
                        startActivity(PlayerActivity.intent(requireContext(), args))
                    }
                    .onBind({ _, view, _ ->
                        view.onBind()
                    })
                    .onUnbind { _, view ->
                        view.onUnbind()
                    }
                    .addTo(controller)
        }
    }
}