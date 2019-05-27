package com.airbnb.lottie.samples.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.support.annotation.FloatRange
import androidx.core.view.children
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.ImageAssetDelegate
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.samples.R
import com.airbnb.lottie.samples.inflate

class FilmStripView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val animationViews by lazy {
        findViewById<ViewGroup>(R.id.grid_layout).children.filterIsInstance(LottieAnimationView::class.java)
    }

    init {
        inflate(R.layout.film_strip_view)
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
}