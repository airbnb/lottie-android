package com.airbnb.lottie.samples.views

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.lottie.samples.R
import com.airbnb.lottie.samples.model.ShowcaseItem

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ShowcaseCarousel @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : EpoxyRecyclerView(context, attrs, defStyleAttr), EpoxyRecyclerView.ModelBuilderCallback {

    private var items: List<ShowcaseItem>? = null

    init {
        layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        buildModelsWith(this)
        val sidePadding = resources.getDimensionPixelSize(R.dimen.showcase_carousel_padding)
        setPadding(sidePadding, 0, sidePadding, 0)
    }

    @ModelProp
    fun setShowcaseItems(items: List<ShowcaseItem>) {
        this.items = items
        requestModelBuild()
    }

    override fun buildModels(controller: EpoxyController) {
        items?.forEach {
            ShowcaseDemoItemViewModel_()
                    .id(it.titleRes)
                    .showcaseItem(it)
                    .addTo(controller)
        }
    }
}