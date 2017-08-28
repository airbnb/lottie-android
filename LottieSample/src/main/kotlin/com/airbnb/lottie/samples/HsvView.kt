package com.airbnb.lottie.samples

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class HsvView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val hsv = floatArrayOf(0f, 1f, 1f)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private lateinit var listener: (Int) -> Unit

    fun setListener(listener: (Int) -> Unit) {
        this.listener = listener
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        for (i in 0 until canvas.width - 1) {
            paint.color = getColorForPercentage(i / canvas.width.toFloat())
            canvas.drawRect(i.toFloat(), 0f, i + 1f, canvas.height.toFloat(), paint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        listener(getColorForPercentage(event.x / width.toFloat()))
        return true
    }

    private fun getColorForPercentage(percentage: Float): Int {
        hsv[0] = percentage * 360
        return Color.HSVToColor(hsv)
    }
}