package com.airbnb.lottie.samples

import android.animation.Animator

internal open class AnimatorListenerAdapter(
        val onStart: ((Animator) -> Unit)? = null,
        val onRepeat: ((Animator) -> Unit)? = null,
        val onEnd: ((Animator) -> Unit)? = null,
        val onCancel: ((Animator) -> Unit)? = null
): Animator.AnimatorListener {

    override fun onAnimationStart(animation: Animator) = onStart?.invoke(animation) ?: Unit
    override fun onAnimationRepeat(animation: Animator) = onRepeat?.invoke(animation) ?: Unit
    override fun onAnimationEnd(animation: Animator) = onEnd?.invoke(animation) ?: Unit
    override fun onAnimationCancel(animation: Animator) = onCancel?.invoke(animation) ?: Unit
}