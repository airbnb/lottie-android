package com.airbnb.lottie.samples

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView

class TrimView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val leftAnchor by lazy {
        ImageView(context).apply { setImageResource(R.drawable.ic_trim) }
    }
    private val rightAnchor by lazy {
        ImageView(context).apply { setImageResource(R.drawable.ic_trim) }
    }
    private lateinit var callback: (Float, Float) -> Unit

    private val dragHelper = ViewDragHelper.create(this, object: ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int) = true

        override fun getViewHorizontalDragRange(child: View) = width

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return if (child == leftAnchor) {
                maxOf(minOf(left, rightAnchor.left - leftAnchor.width), 0)
            } else {
                minOf(maxOf(leftAnchor.right, left), width - rightAnchor.width)
            }
        }

        override fun onViewPositionChanged(view: View, left: Int, top: Int, dx: Int, dy: Int) {
            val startProgress = leftAnchor.left / width.toFloat()
            val endProgress = rightAnchor.right / width.toFloat()
            callback(startProgress, endProgress)
        }
    })


    init {
        leftAnchor.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.START
        }
        addView(leftAnchor)

        rightAnchor.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.END
        }
        addView(rightAnchor)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (dragHelper.shouldInterceptTouchEvent(ev)) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        dragHelper.processTouchEvent(event)
        return true
    }

    fun setCallback(callback: (Float, Float) -> Unit) {
        this.callback = callback
    }
}