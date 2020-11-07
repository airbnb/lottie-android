package com.airbnb.lottie.samples.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.lottie.samples.databinding.ItemViewShowcaseDemoBinding
import com.airbnb.lottie.samples.model.ShowcaseItem
import com.airbnb.lottie.samples.utils.viewBinding

@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_WRAP_HEIGHT)
class ShowcaseDemoItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: ItemViewShowcaseDemoBinding by viewBinding()

    @ModelProp
    fun setShowcaseItem(item: ShowcaseItem) {
        binding.imageView.setImageResource(item.drawableRes)

        binding.titleView.text = resources.getText(item.titleRes)

        binding.cardView.setOnClickListener { item.clickListener() }
    }
}