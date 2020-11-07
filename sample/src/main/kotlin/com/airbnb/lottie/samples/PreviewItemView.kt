package com.airbnb.lottie.samples

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import com.airbnb.lottie.samples.databinding.ListItemPreviewBinding
import com.airbnb.lottie.samples.utils.viewBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class PreviewItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val binding: ListItemPreviewBinding by viewBinding()

    init {
        orientation = VERTICAL
    }

    @TextProp
    fun setTitle(title: CharSequence) {
        binding.titleView.text = title
    }

    @ModelProp
    fun setIcon(@DrawableRes icon: Int) {
        binding.iconView.setImageResource(icon)
    }

    @CallbackProp
    fun setClickListener(clickListener: OnClickListener?) {
        binding.container.setOnClickListener(clickListener)
    }
}