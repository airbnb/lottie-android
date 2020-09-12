package com.airbnb.lottie.samples.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import com.airbnb.lottie.samples.R
import com.airbnb.lottie.samples.databinding.ItemViewShowcaseAnimationBinding
import com.airbnb.lottie.samples.utils.setImageUrl
import com.airbnb.lottie.samples.utils.viewBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class AnimationItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: ItemViewShowcaseAnimationBinding by viewBinding()

    @ModelProp
    fun setPreviewUrl(url: String?) {
        binding.imageView.setImageUrl(url)
    }

    @TextProp
    fun setTitle(title: CharSequence?) {
        binding.titleView.text = title
    }

    @ModelProp
    fun setPreviewBackgroundColor(@ColorInt bgColor: Int?) {
        if (bgColor == null) {
            binding.imageView.setBackgroundResource(R.color.loading_placeholder)
            binding.imageView.setImageDrawable(null)
        } else {
            binding.imageView.setBackgroundColor(bgColor)
        }
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    override fun setOnClickListener(l: OnClickListener?) {
        binding.container.setOnClickListener(l)
    }
}