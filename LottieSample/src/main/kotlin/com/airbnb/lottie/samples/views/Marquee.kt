package com.airbnb.lottie.samples.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import com.airbnb.lottie.samples.R
import com.airbnb.lottie.samples.getText
import com.airbnb.lottie.samples.inflate
import kotlinx.android.synthetic.main.marquee.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class Marquee @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        inflate(R.layout.marquee)
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.Marquee, 0, 0)

            val titleRes = typedArray.getResourceId(R.styleable.Marquee_titleText, 0)
            if (titleRes != 0) {
                titleView.text = getText(titleRes)
            }

            typedArray.recycle()
        }
    }

    @TextProp
    fun setTitle(title: CharSequence) {
        titleView.text = title
    }
}