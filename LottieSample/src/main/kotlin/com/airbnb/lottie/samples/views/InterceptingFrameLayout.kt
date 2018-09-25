package com.airbnb.lottie.samples.views

import android.content.Context
import androidx.customview.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class InterceptingFrameLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var viewDragHelper: ViewDragHelper? = null

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (viewDragHelper?.shouldInterceptTouchEvent(ev) == true) return true
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        viewDragHelper?.processTouchEvent(event)
        return true
    }
}