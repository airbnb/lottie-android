package com.airbnb.lottie.samples.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.airbnb.lottie.samples.R

class BackgroundColorView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleInt: Int = 0
) : View(context, attrs, defStyleInt) {

    private val paint = Paint().apply {
        isAntiAlias = true
    }


    @SuppressLint("MissingSuperCall")
    override fun draw(canvas: Canvas) {
        if (background !is ColorDrawable) return

        val cx = canvas.width / 2f
        val cy = canvas.height / 2f
        val r = Math.min(cx, cy)
        if (getColor() == Color.WHITE) {
            paint.strokeWidth =
                    resources.getDimensionPixelSize(R.dimen.background_color_view_stroke_width).toFloat()
            paint.style = Paint.Style.STROKE
            paint.color = ContextCompat.getColor(context, R.color.background_color1_stroke)
        } else {
            paint.color = getColor()
            paint.style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, r - paint.strokeWidth, paint)
    }

    @ColorInt fun getColor() = (background as ColorDrawable).color
}