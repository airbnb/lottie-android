package com.airbnb.lottie.samples

import android.annotation.SuppressLint
import android.content.Context
import androidx.customview.widget.ViewDragHelper
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
        val iv = ImageView(context)
        iv.setImageResource(R.drawable.ic_trim)
        iv
    }
    private val rightAnchor by lazy {
        val iv = ImageView(context)
        iv.setImageResource(R.drawable.ic_trim)
        iv
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
        val leftLp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        leftLp.gravity = Gravity.START
        leftAnchor.layoutParams = leftLp
        addView(leftAnchor)
        val rightLp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        rightLp.gravity = Gravity.END
        rightAnchor.layoutParams = rightLp
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