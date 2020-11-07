package com.airbnb.lottie.samples.testing

import android.content.Context
import android.util.AttributeSet
import com.airbnb.lottie.LottieAnimationView

class NoCacheLottieAnimationView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LottieAnimationView(context, attrs, defStyleAttr) {

    override fun buildDrawingCache(autoScale: Boolean) {
        // Prevent the cache from getting generated.
    }
}