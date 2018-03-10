package com.airbnb.lottie.samples.views

import android.content.Context
import android.graphics.Color
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.view.isVisible
import androidx.view.setPadding
import com.airbnb.lottie.samples.R
import com.airbnb.lottie.samples.getText
import kotlinx.android.synthetic.main.item_view_control_bar.view.*

class ControlBarItemToggleView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.item_view_control_bar, this)
        orientation = HORIZONTAL
        setBackgroundResource(R.drawable.control_bar_item_view_background)
        setPadding(resources.getDimensionPixelSize(R.dimen.control_bar_button_padding))

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ControlBarItemToggleView, 0, 0)

            val textRes = typedArray.getResourceId(R.styleable.ControlBarItemToggleView_text, 0)
            if (textRes != 0) {
                textView.text = getText(textRes)
            }

            val drawableRes = typedArray.getResourceId(R.styleable.ControlBarItemToggleView_src, 0)
            if (drawableRes == 0) {
                imageView.isVisible = false
            } else {
                imageView.setImageResource(drawableRes)
            }

            typedArray.recycle()
        }
    }

    override fun childDrawableStateChanged(child: View) {
        super.childDrawableStateChanged(child)
        if (child is ImageView && child.drawable != null) {
            val color =
                    if (child.isActivated) Color.WHITE
                    else ContextCompat.getColor(context, R.color.control_bar_content_unactivated)
            DrawableCompat.setTint(child.drawable.mutate(), color)
        }
    }

    fun getText() = textView.text.toString()

    fun setText(text: String) {
        textView.text = text
    }

    fun setImageResource(@DrawableRes drawableRes: Int) {
        imageView.setImageResource(drawableRes)
        childDrawableStateChanged(imageView)
    }
}