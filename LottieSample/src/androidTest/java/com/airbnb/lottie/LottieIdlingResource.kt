package com.airbnb.lottie

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource

class LottieIdlingResource(animationView: LottieAnimationView? = null, private val name: String = "Lottie") : IdlingResource {

    init {
        if (animationView != null) {
            animationView.addLottieOnCompositionLoadedListener {
                isIdle = true
                callback?.onTransitionToIdle()
                IdlingRegistry.getInstance().unregister(this)
            }
        }
    }

    private var callback: IdlingResource.ResourceCallback? = null
    private var isIdle = false

    override fun getName() = name

    override fun isIdleNow() = isIdle

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        this.callback = callback
        if (isIdle) callback.onTransitionToIdle()
    }
}