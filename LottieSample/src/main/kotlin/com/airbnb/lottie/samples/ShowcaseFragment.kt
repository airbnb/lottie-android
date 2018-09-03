package com.airbnb.lottie.samples

import android.content.Intent
import android.support.v4.app.FragmentActivity
import com.airbnb.epoxy.EpoxyController
import com.airbnb.lottie.samples.model.AnimationResponse
import com.airbnb.lottie.samples.model.CompositionArgs
import com.airbnb.lottie.samples.model.ShowcaseItem
import com.airbnb.lottie.samples.views.loadingView
import com.airbnb.lottie.samples.views.marquee
import com.airbnb.lottie.samples.views.showcaseAnimationItemView
import com.airbnb.lottie.samples.views.showcaseCarousel
import com.airbnb.mvrx.*

data class ShowcaseState(val response: Async<AnimationResponse> = Uninitialized) : MvRxState

class ShowcaseViewModel(initialState: ShowcaseState, service: LottiefilesService) : MvRxViewModel<ShowcaseState>(initialState) {
    init {
        service.getCollection("lottie-showcase")
                .retry(3)
                .execute { copy(response = it) }
    }

    companion object : MvRxViewModelFactory<ShowcaseState> {
        @JvmStatic
        override fun create(activity: FragmentActivity, state: ShowcaseState): ShowcaseViewModel {
            val service = (activity.applicationContext as LottieApplication).lottiefilesService
            return ShowcaseViewModel(state, service)
        }
    }
}

class ShowcaseFragment : BaseEpoxyFragment() {

    private val showcaseItems = listOf(
            ShowcaseItem(R.drawable.showcase_preview_lottie, R.string.showcase_item_app_intro) {
                startActivity(Intent(requireContext(), AppIntroActivity::class.java))
            },
            ShowcaseItem(R.drawable.showcase_preview_lottie, R.string.showcase_item_dynamic_properties) {
                startActivity(Intent(requireContext(), DynamicActivity::class.java))
            },
            ShowcaseItem(R.drawable.gilbert_animated, R.string.showcase_item_animated_text) {
                startActivity(Intent(requireContext(), TypographyDemoActivity::class.java))
            },
            ShowcaseItem(R.drawable.gilbert_animated, R.string.showcase_item_dynamic_text) {
                startActivity(Intent(requireContext(), DynamicTextActivity::class.java))
            },
            ShowcaseItem(R.drawable.showcase_preview_lottie, R.string.showcase_item_bullseye) {
                startActivity(Intent(requireContext(), BullseyeActivity::class.java))
            },
            ShowcaseItem(R.drawable.showcase_preview_lottie, R.string.showcase_item_recycler_viwe) {
                startActivity(Intent(requireContext(), ListActivity::class.java))
            }
    )
    private val viewModel: ShowcaseViewModel by fragmentViewModel()

    override fun EpoxyController.buildModels() = withState(viewModel) { state ->
        marquee {
            id("showcase")
            title("Showcase")
        }
        showcaseCarousel {
            id("carousel")
            showcaseItems(showcaseItems)
        }

        val collectionItems = state.response()?.data

        if (collectionItems == null) {
            loadingView {
                id("loading")
            }
        } else {
            collectionItems.forEach {
                showcaseAnimationItemView {
                    id(it.id)
                    title(it.title)
                    previewUrl(it.preview)
                    onClickListener { _ -> startActivity(PlayerActivity.intent(requireContext(), CompositionArgs(animationData = it))) }
                }
            }
        }
    }
}