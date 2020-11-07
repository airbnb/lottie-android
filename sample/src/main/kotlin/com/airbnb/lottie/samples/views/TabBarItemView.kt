package com.airbnb.lottie.samples.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.airbnb.lottie.samples.R
import com.airbnb.lottie.samples.databinding.TabItemBinding
import com.airbnb.lottie.samples.utils.viewBinding

class TabBarItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val binding: TabItemBinding by viewBinding()

    init {
        orientation = VERTICAL

        context.withStyledAttributes(attrs, R.styleable.TabBarItemView) {
            if (hasValue(R.styleable.TabBarItemView_titleText)) {
                binding.titleView.text = getText(R.styleable.TabBarItemView_titleText)
            }
        }
    }
}