package com.airbnb.lottie.samples.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.airbnb.lottie.samples.R
import com.airbnb.lottie.samples.databinding.ItemViewControlBarBinding
import com.airbnb.lottie.samples.utils.getText
import com.airbnb.lottie.samples.utils.viewBinding

class ControlBarItemToggleView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val binding: ItemViewControlBarBinding by viewBinding()

    init {
        orientation = HORIZONTAL
        setBackgroundResource(R.drawable.control_bar_item_view_background)
        setPadding(resources.getDimensionPixelSize(R.dimen.control_bar_button_padding))
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ControlBarItemToggleView, 0, 0)

            val textRes = typedArray.getResourceId(R.styleable.ControlBarItemToggleView_text, 0)
            if (textRes != 0) {
                binding.textView.text = getText(textRes)
            }

            val drawableRes = typedArray.getResourceId(R.styleable.ControlBarItemToggleView_src, 0)
            if (drawableRes == 0) {
                binding.imageView.isVisible = false
            } else {
                binding.imageView.setImageResource(drawableRes)
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

    fun getText() = binding.textView.text.toString()

    fun setText(text: String) {
        binding.textView.text = text
    }

    fun setImageResource(@DrawableRes drawableRes: Int) {
        binding.imageView.setImageResource(drawableRes)
        childDrawableStateChanged(binding.imageView)
    }
}