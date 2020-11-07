package com.airbnb.lottie.samples.views

import android.content.Context
import android.util.AttributeSet
import com.airbnb.lottie.LottieAnimationView

class WishListIconView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LottieAnimationView(context, attrs, defStyleAttr) {

    fun toggleWishlisted() {
        isActivated = !isActivated
    }

    override fun setActivated(activated: Boolean) {
        super.setActivated(activated)
        speed = if (activated) 1f else -2f
        progress = 0f
        playAnimation()
    }
}