package com.airbnb.lottie.samples.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import com.airbnb.lottie.samples.R
import com.airbnb.lottie.samples.getText
import com.airbnb.lottie.samples.inflate
import com.airbnb.lottie.samples.setVisibleIf
import kotlinx.android.synthetic.main.marquee.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class Marquee @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        inflate(R.layout.marquee)
        orientation = VERTICAL
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.Marquee, 0, 0)

            val titleRes = typedArray.getResourceId(R.styleable.Marquee_titleText, 0)
            if (titleRes != 0) {
                setTitle(getText(titleRes))
            }

            val subtitleRes = typedArray.getResourceId(R.styleable.Marquee_subtitleText, 0)
            if (subtitleRes != 0) {
                setSubtitle(getText(subtitleRes))
            }

            typedArray.recycle()
        }
    }

    @TextProp
    fun setTitle(title: CharSequence) {
        titleView.text = title
    }

    @TextProp
    fun setSubtitle(subtitle: CharSequence?) {
        subtitleView.text = subtitle
        subtitleView.setVisibleIf(!subtitle.isNullOrEmpty())
    }
}