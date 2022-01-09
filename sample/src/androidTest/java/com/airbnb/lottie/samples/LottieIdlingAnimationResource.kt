package com.airbnb.lottie.samples

import android.animation.Animator
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import com.airbnb.lottie.LottieAnimationView

class LottieIdlingAnimationResource(animationView: LottieAnimationView, private val name: String = "Lottie") : IdlingResource {

    private var hasEnded = false
    private var callback: IdlingResource.ResourceCallback? = null

    init {
        animationView.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                hasEnded = true
                callback?.onTransitionToIdle()
                animationView.removeAllAnimatorListeners()
                IdlingRegistry.getInstance().unregister(this@LottieIdlingAnimationResource)
            }
        })
    }


    override fun getName() = name

    override fun isIdleNow() = hasEnded

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.callback = callback
        if (isIdleNow) callback?.onTransitionToIdle()
    }
}