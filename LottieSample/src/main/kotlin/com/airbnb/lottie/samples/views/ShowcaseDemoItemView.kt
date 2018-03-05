package com.airbnb.lottie.samples.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.lottie.samples.R
import com.airbnb.lottie.samples.inflate
import com.airbnb.lottie.samples.model.ShowcaseItem
import kotlinx.android.synthetic.main.item_view_showcase_demo.view.*

@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_WRAP_HEIGHT)
class ShowcaseDemoItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        inflate(R.layout.item_view_showcase_demo)
    }

    @ModelProp
    fun setShowcaseItem(item: ShowcaseItem) {
        imageView.setImageResource(item.drawableRes)

        titleView.text = resources.getText(item.titleRes)

        cardView.setOnClickListener({ item.clickListener() })
    }
}