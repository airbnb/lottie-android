package com.airbnb.lottie.samples.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import com.airbnb.lottie.samples.R
import com.airbnb.lottie.samples.inflate
import com.airbnb.lottie.samples.setImageUrl
import kotlinx.android.synthetic.main.item_view_showcase_animation.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class AnimationItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        inflate(R.layout.item_view_showcase_animation)
    }

    @ModelProp
    fun setPreviewUrl(url: String?) {
        imageView.setImageUrl(url)
    }

    @TextProp
    fun setTitle(title: CharSequence?) {
        titleView.text = title
    }

    @ModelProp
    fun setPreviewBackgroundColor(@ColorInt bgColor: Int?) {
        if (bgColor == null) {
            imageView.setBackgroundResource(R.color.loading_placeholder)
            imageView.setImageDrawable(null)
        } else {
            imageView.setBackgroundColor(bgColor)
        }
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    override fun setOnClickListener(l: OnClickListener?) {
        container.setOnClickListener(l)
    }
}