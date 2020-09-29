package com.airbnb.lottie.samples.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.lottie.samples.LottiefilesMode
import com.airbnb.lottie.samples.databinding.LottiefilesTabBarBinding
import com.airbnb.lottie.samples.utils.viewBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class LottiefilesTabBar @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val binding: LottiefilesTabBarBinding by viewBinding()

    @ModelProp
    fun setMode(mode: LottiefilesMode) {
        binding.popularView.isActivated = mode == LottiefilesMode.Popular
        binding.recentView.isActivated = mode == LottiefilesMode.Recent
        binding.searchView.isActivated = mode == LottiefilesMode.Search
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setPopularClickListener(listener: View.OnClickListener) {
        binding.popularView.setOnClickListener(listener)
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setRecentClickListener(listener: OnClickListener) {
        binding.recentView.setOnClickListener(listener)
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setSearchClickListener(listener: OnClickListener) {
        binding.searchView.setOnClickListener(listener)
    }
}