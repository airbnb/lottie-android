package com.airbnb.lottie.samples.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.children
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.ImageAssetDelegate
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.samples.databinding.FilmStripViewBinding
import com.airbnb.lottie.samples.utils.viewBinding

class FilmStripView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: FilmStripViewBinding by viewBinding()

    private val animationViews by lazy {
        binding.gridLayout.children.filterIsInstance(LottieAnimationView::class.java)
    }

    fun setComposition(composition: LottieComposition) {
        animationViews.forEachIndexed { i, view ->
            view.setComposition(composition)
            view.progress = i / 24f
        }
    }

    fun setImageAssetDelegate(delegate: ImageAssetDelegate) {
        animationViews.forEach { it.setImageAssetDelegate(delegate) }
    }

    fun setFontAssetDelegate(delegate: FontAssetDelegate) {
        animationViews.forEach { it.setFontAssetDelegate(delegate) }
    }

    fun setApplyingOpacityToLayersEnabled(isApplyingOpacityToLayersEnabled: Boolean) {
        animationViews.forEach { it.setApplyingOpacityToLayersEnabled(isApplyingOpacityToLayersEnabled) }
    }

    fun setOutlineMasksAndMattes(outline: Boolean) {
        animationViews.forEach { it.setOutlineMasksAndMattes(outline) }
    }
}
