package com.airbnb.lottie.samples.views

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.lottie.samples.databinding.ItemViewBottomSheetBinding
import com.airbnb.lottie.samples.utils.viewBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class BottomSheetItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: ItemViewBottomSheetBinding by viewBinding()

    @SuppressLint("SetTextI18n")
    fun set(left: String, right: String? = null) {
        binding.leftTextView.text = left
        binding.rightTextView.isVisible = !TextUtils.isEmpty(right)
        binding.rightTextView.text = right
    }

    @ModelProp
    fun setText(text: String) {
        set(text)
    }
}