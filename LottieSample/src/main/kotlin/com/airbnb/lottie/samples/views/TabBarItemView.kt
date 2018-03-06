package com.airbnb.lottie.samples.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.airbnb.lottie.samples.R
import com.airbnb.lottie.samples.getText
import com.airbnb.lottie.samples.inflate
import kotlinx.android.synthetic.main.tab_item.view.*

class TabBarItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        inflate(R.layout.tab_item)
        orientation = VERTICAL

        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.TabBarItemView, 0, 0)

            val titleRes = ta.getResourceId(R.styleable.TabBarItemView_titleText, 0)
            if (titleRes != 0) {
                titleView.text = getText(titleRes)
            }

            ta.recycle()
        }
    }
}