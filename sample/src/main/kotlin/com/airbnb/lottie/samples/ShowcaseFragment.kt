package com.airbnb.lottie.samples

import android.app.Activity
import android.content.Intent
import com.airbnb.epoxy.EpoxyController
import com.airbnb.lottie.samples.api.LottiefilesApi
import com.airbnb.lottie.samples.model.AnimationResponseV2
import com.airbnb.lottie.samples.model.CompositionArgs
import com.airbnb.lottie.samples.model.ShowcaseItem
import com.airbnb.lottie.samples.utils.BaseEpoxyFragment
import com.airbnb.lottie.samples.views.animationItemView
import com.airbnb.lottie.samples.views.loadingView
import com.airbnb.lottie.samples.views.marquee
import com.airbnb.lottie.samples.views.showcaseCarousel
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState

data class ShowcaseState(val response: Async<AnimationResponseV2> = Uninitialized) : MavericksState

class ShowcaseViewModel(initialState: ShowcaseState, api: LottiefilesApi) : MavericksViewModel<ShowcaseState>(initialState) {
    init {
        suspend {
            api.getCollection()
        }.execute { copy(response = it) }
    }

    companion object : MavericksViewModelFactory<ShowcaseViewModel, ShowcaseState> {
        override fun create(viewModelContext: ViewModelContext, state: ShowcaseState): ShowcaseViewModel {
            val service = viewModelContext.app<LottieApplication>().lottiefilesService
            return ShowcaseViewModel(state, service)
        }
    }
}

class ShowcaseFragment : BaseEpoxyFragment() {

    private inline fun <reified A : Activity> startActivity() {
        val intent = Intent(requireContext(), A::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        requireContext().startActivity(intent)
    }

    private val showcaseItems = listOf(
        ShowcaseItem(R.drawable.showcase_preview_lottie, R.string.showcase_item_dynamic_properties) {
            startActivity<DynamicActivity>()
        },
        ShowcaseItem(R.drawable.gilbert_animated, R.string.showcase_item_animated_text) {
            startActivity<TypographyDemoActivity>()
        },
        ShowcaseItem(R.drawable.gilbert_animated, R.string.showcase_item_dynamic_text) {
            startActivity<DynamicTextActivity>()
        },
        ShowcaseItem(R.drawable.showcase_preview_lottie, R.string.showcase_item_bullseye) {
            startActivity<BullseyeActivity>()
        },
        ShowcaseItem(R.drawable.showcase_preview_lottie, R.string.showcase_item_recycler_view) {
            startActivity<WishListActivity>()
        },
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
                val activityContext = requireActivity()
                animationItemView {
                    id(it.id)
                    title(it.title)
                    if (it.preview != null) previewUrl("https://assets9.lottiefiles.com/${it.preview}")
                    previewBackgroundColor(it.bgColorInt)
                    onClickListener { _ ->
                        activityContext.startActivity(
                            PlayerActivity.intent(
                                activityContext,
                                CompositionArgs(animationDataV2 = it),
                            ),
                        )
                    }
                }
            }
        }
    }
}
