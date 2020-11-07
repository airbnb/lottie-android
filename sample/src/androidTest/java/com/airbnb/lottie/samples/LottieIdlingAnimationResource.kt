package com.airbnb.lottie.samples

import android.animation.Animator
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import com.airbnb.lottie.LottieAnimationView

class LottieIdlingAnimationResource(animationView: LottieAnimationView?, private val name: String = "Lottie") : IdlingResource {

    init {
        animationView?.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                isIdle = false
            }

            override fun onAnimationEnd(animation: Animator) {
                isIdle = true
                callback?.onTransitionToIdle()
                animationView.removeAllAnimatorListeners()
                IdlingRegistry.getInstance().unregister(this@LottieIdlingAnimationResource)
            }
        })
    }

    private var callback: IdlingResource.ResourceCallback? = null
    private var isIdle = animationView?.isAnimating?.not() ?: true


    override fun getName() = name

    override fun isIdleNow() = isIdle

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.callback = callback
        if (isIdle) callback?.onTransitionToIdle()
    }
}