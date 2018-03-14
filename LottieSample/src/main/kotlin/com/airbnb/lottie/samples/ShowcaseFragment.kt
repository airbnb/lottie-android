package com.airbnb.lottie.samples

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.lottie.samples.model.CompositionArgs
import com.airbnb.lottie.samples.model.ShowcaseItem
import com.airbnb.lottie.samples.views.LoadingViewModel_
import com.airbnb.lottie.samples.views.MarqueeModel_
import com.airbnb.lottie.samples.views.ShowcaseAnimationItemViewModel_
import com.airbnb.lottie.samples.views.ShowcaseCarouselModel_
import kotlinx.android.synthetic.main.fragment_epoxy_recycler_view.*

class ShowcaseFragment : Fragment(), EpoxyRecyclerView.ModelBuilderCallback {

    private val showcaseItems = listOf(
            ShowcaseItem(R.drawable.showcase_preview_lottie, R.string.showcase_item_app_intro) {
                startActivity(Intent(requireContext(), AppIntroActivity::class.java))
            },
            ShowcaseItem(R.drawable.showcase_preview_lottie, R.string.showcase_item_bullseye) {
                startActivity(Intent(requireContext(), BullseyeActivity::class.java))
            },
            ShowcaseItem(R.drawable.showcase_preview_lottie, R.string.showcase_item_dynamic_properties) {
                startActivity(Intent(requireContext(), DynamicActivity::class.java))
            },
            ShowcaseItem(R.drawable.gilbert_animated, R.string.showcase_item_animated_text) {
                startActivity(Intent(requireContext(), TypographyDemoActivity::class.java))
            },
            ShowcaseItem(R.drawable.gilbert_animated, R.string.showcase_item_dynamic_text) {
                startActivity(Intent(requireContext(), DynamicTextActivity::class.java))
            }
    )
    private val viewModel by lazy { ViewModelProviders.of(this).get(ShowcaseViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_epoxy_recycler_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.buildModelsWith(this)

        viewModel.collection.observe(this, Observer {
            recyclerView.requestModelBuild()
        })

        viewModel.loading.observe(this, Observer {
            recyclerView.requestModelBuild()
        })

        viewModel.fetchAnimations()
    }

    override fun buildModels(controller: EpoxyController) {
        MarqueeModel_()
                .id("showcase")
                .title("Showcase")
                .addTo(controller)
        ShowcaseCarouselModel_()
                .id("carousel")
                .showcaseItems(showcaseItems)
                .addTo(controller)

        viewModel.collection.value?.data?.forEach {
            ShowcaseAnimationItemViewModel_()
                    .id(it.id)
                    .title(it.title)
                    .previewUrl(it.preview)
                    .onClickListener { _ ->
                        startActivity(PlayerActivity.intent(requireContext(), CompositionArgs(animationData = it)))
                    }
                    .addTo(controller)
        }

        LoadingViewModel_()
                .id("loading")
                .addIf(viewModel.loading.value ?: false, controller)
    }
}