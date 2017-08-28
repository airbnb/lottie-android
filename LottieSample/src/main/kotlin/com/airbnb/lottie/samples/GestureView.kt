package com.airbnb.lottie.samples

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class GestureView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var listener: ((event: MotionEvent) -> Unit)? = null

    fun setListener(listener: ((event: MotionEvent) -> Unit)?) {
        this.listener = listener
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        listener?.invoke(event)
        return true
    }
}