package com.airbnb.lottie.samples

import androidx.test.espresso.IdlingResource
import com.airbnb.lottie.LottieCompositionFactory

class LottieIdlingResource(private val name: String = "Lottie") : IdlingResource {

    private var callback: IdlingResource.ResourceCallback? = null
    private var isIdle = false

    init {
        LottieCompositionFactory.registerLottieTaskIdleListener { idle ->
            isIdle = idle
            if (idle) {
                callback?.onTransitionToIdle()
            }
        }
    }

    override fun getName() = name

    override fun isIdleNow() = isIdle

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        this.callback = callback
        if (isIdle) callback.onTransitionToIdle()
    }
}