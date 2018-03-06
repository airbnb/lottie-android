package com.airbnb.lottie.samples.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.lottie.samples.LottiefilesMode
import com.airbnb.lottie.samples.R
import com.airbnb.lottie.samples.inflate
import kotlinx.android.synthetic.main.lottiefiles_tab_bar.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class LottiefilesTabBar @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        inflate(R.layout.lottiefiles_tab_bar)
    }

    @ModelProp
    fun setMode(mode: LottiefilesMode) {
        popularView.isActivated = mode == LottiefilesMode.Popular
        recentView.isActivated = mode == LottiefilesMode.Recent
        searchView.isActivated = mode == LottiefilesMode.Search
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setPopularClickListener(listener: View.OnClickListener) {
        popularView.setOnClickListener(listener)
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setRecentClickListener(listener: View.OnClickListener) {
        recentView.setOnClickListener(listener)
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setSearchClickListener(listener: View.OnClickListener) {
        searchView.setOnClickListener(listener)
    }
}