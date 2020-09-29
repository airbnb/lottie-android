package com.airbnb.lottie.samples

import android.content.Intent
import com.airbnb.epoxy.EpoxyController
import com.airbnb.lottie.samples.api.LottiefilesApi
import com.airbnb.lottie.samples.model.AnimationResponseV2
import com.airbnb.lottie.samples.model.CompositionArgs
import com.airbnb.lottie.samples.model.ShowcaseItem
import com.airbnb.lottie.samples.utils.BaseEpoxyFragment
import com.airbnb.lottie.samples.utils.MvRxViewModel
import com.airbnb.lottie.samples.views.animationItemView
import com.airbnb.lottie.samples.views.loadingView
import com.airbnb.lottie.samples.views.marquee
import com.airbnb.lottie.samples.views.showcaseCarousel
import com.airbnb.mvrx.*
import kotlinx.coroutines.Dispatchers

data class ShowcaseState(val response: Async<AnimationResponseV2> = Uninitialized) : MvRxState

class ShowcaseViewModel(initialState: ShowcaseState, api: LottiefilesApi) : MvRxViewModel<ShowcaseState>(initialState) {
    init {
        suspend {
            api.getCollection()
        }.execute(Dispatchers.IO) { copy(response = it) }
    }

    companion object : MvRxViewModelFactory<ShowcaseViewModel, ShowcaseState> {
        override fun create(viewModelContext: ViewModelContext, state: ShowcaseState): ShowcaseViewModel? {
            val service = viewModelContext.app<LottieApplication>().lottiefilesService
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
            ShowcaseItem(R.drawable.showcase_preview_lottie, R.string.showcase_item_recycler_view) {
                startActivity(Intent(requireContext(), WishListActivity::class.java))
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
                animationItemView {
                    id(it.id)
                    title(it.title)
                    previewUrl("https://assets9.lottiefiles.com/${it.preview}")
                    previewBackgroundColor(it.bgColorInt)
                    onClickListener { _ -> startActivity(PlayerActivity.intent(requireContext(), CompositionArgs(animationDataV2 = it))) }
                }
            }
        }
    }
}