package com.airbnb.lottie.snapshots

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.core.view.children
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.ImageAssetDelegate
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.snapshots.databinding.FilmStripViewBinding
import com.airbnb.lottie.snapshots.utils.viewBinding
import kotlin.math.round

class FilmStripView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: FilmStripViewBinding by viewBinding()

    private val animationViews = binding.gridLayout.children.filterIsInstance<LottieAnimationView>()

    fun setComposition(composition: LottieComposition, name: String) {
        animationViews.forEachIndexed { i, view ->
            view.setComposition(composition)
            val progress = (i / 8f).round(decimals = 2)
            view.progress = progress
            Log.d("FilmStripView", "$name ${composition.hashCode} $i $progress -> ${view.progress}")
        }
    }

    fun setImageAssetDelegate(delegate: ImageAssetDelegate?) {
        animationViews.forEach { it.setImageAssetDelegate(delegate) }
    }

    fun setFontAssetDelegate(delegate: FontAssetDelegate?) {
        animationViews.forEach { it.setFontAssetDelegate(delegate) }
    }

    fun setApplyingOpacityToLayersEnabled(isApplyingOpacityToLayersEnabled: Boolean) {
        animationViews.forEach { it.setApplyingOpacityToLayersEnabled(isApplyingOpacityToLayersEnabled) }
    }

    fun setOutlineMasksAndMattes(outline: Boolean) {
        animationViews.forEach { it.setOutlineMasksAndMattes(outline) }
    }

    private fun Float.round(decimals: Int): Float {
        var multiplier = 1f
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }
}