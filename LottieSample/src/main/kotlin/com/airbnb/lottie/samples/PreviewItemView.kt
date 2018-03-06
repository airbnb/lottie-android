package com.airbnb.lottie.samples

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.list_item_preview.view.*

class PreviewItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = VERTICAL
        inflate(R.layout.list_item_preview)

        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.PreviewItemView)
            val titleText = resources.getText(ta.getResourceId(R.styleable.PreviewItemView_titleText, 0))
            val iconRes = ta.getResourceId(R.styleable.PreviewItemView_icon, 0)

            titleView.text = titleText
            iconView.setImageResource(iconRes)

            ta.recycle()
        }

        val outValue = TypedValue()
        getContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        setBackgroundResource(outValue.resourceId)
    }
}