package com.airbnb.lottie.samples

import android.content.Context
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.ImageView
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
            val typedArray = context.obtainStyledAttributes(it, R.styleable.PreviewItemView, 0, 0)
            val titleText = resources.getText(typedArray.getResourceId(R.styleable.PreviewItemView_titleText, 0))
            val actionText = resources.getText(typedArray.getResourceId(R.styleable.PreviewItemView_actionText, 0))

            title.text = titleText
            action.text = actionText

            typedArray.recycle()
        }

        val outValue = TypedValue()
        getContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        setBackgroundResource(outValue.resourceId)
    }

    fun addIcon(@DrawableRes drawableRes: Int) {
        val imageView = ImageView(context)
        imageView.setImageResource(drawableRes)
        icons.addView(imageView)
    }
}