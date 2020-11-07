package com.airbnb.lottie.samples.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import com.airbnb.lottie.samples.databinding.SectionHeaderViewBinding
import com.airbnb.lottie.samples.utils.viewBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class SectionHeaderView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: SectionHeaderViewBinding by viewBinding()

    @TextProp
    fun setTitle(title: CharSequence) {
        binding.titleView.text = title
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    override fun setOnClickListener(listener: OnClickListener?) {
        super.setOnClickListener(listener)
    }
}