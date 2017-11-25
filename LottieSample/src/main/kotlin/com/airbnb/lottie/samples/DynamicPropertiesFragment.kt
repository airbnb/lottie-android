package com.airbnb.lottie.samples

import android.graphics.PointF
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.value.*
import kotlinx.android.synthetic.main.fragment_dynamic_properties.*
import kotlinx.android.synthetic.main.fragment_dynamic_properties.view.*

class DynamicPropertiesFragment : Fragment() {

    private data class Square(
            val position: PointFValue, val positionKeyPath: KeyPath,
            val scale: ScaleValue, val scaleKeyPath: KeyPath)

    private val squares = listOf(
        Square(
                PointFValue.forPosition(PointF()),
                KeyPath("Shape Layer 1", "Group 1"),
                ScaleValue.forScale(ScaleXY()),
                KeyPath("Shape Layer 1", "Group 1", "Rectangle")),
        Square(
                PointFValue.forPosition(PointF()),
                KeyPath("Shape Layer 2", "Group 1"),
                ScaleValue.forScale(ScaleXY()),
                KeyPath("Shape Layer 2", "Group 1", "Rectangle")),
        Square(
                PointFValue.forPosition(PointF()),
                KeyPath("Shape Layer 1", "Group 2"),
                ScaleValue.forScale(ScaleXY()),
                KeyPath("Shape Layer 1", "Group 2", "Rectangle")),
        Square(
                PointFValue.forPosition(PointF()),
                KeyPath("Shape Layer 2", "Group 2"),
                ScaleValue.forScale(ScaleXY()),
                KeyPath("Shape Layer 2", "Group 2", "Rectangle")))

    private val wildcardKeyPath = KeyPath("*")
    private var currentSquare: Square? = null

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = container!!.inflate(R.layout.fragment_dynamic_properties)

        view.gestureView.setListener { e ->
            if (e.action == MotionEvent.ACTION_DOWN) {
                updateSquare(e)
            } else if (e.action == MotionEvent.ACTION_MOVE && e.historySize > 0) {
                val dx = (e.x - e.getHistoricalX(0))
                val dy = (e.y - e.getHistoricalY(0))
                val square = currentSquare!!

                square.position.offsetValue(dx, dy)
                animationView.setValue(square.position, square.positionKeyPath)

                val pos = square.position.value
                val dScale = Math.hypot(pos.x.toDouble(), pos.y.toDouble()).toFloat()
                square.scale.setValue(1 + dScale / 700f)
                animationView.setValue(square.scale, square.scaleKeyPath)
            }
        }

        view.hsvView.setListener { c ->
            val value = ColorFilterValue.forColor(SimpleColorFilter(c))
            animationView.setValue(value, wildcardKeyPath)
        }

        return view
    }

    private fun updateSquare(e: MotionEvent) {
        val cx = gestureView.width / 2
        val cy = gestureView.height / 2
        val x = e.x
        val y = e.y

        currentSquare = if (x < cx) {
            if (y < cy) squares[0] else squares[1]
        } else {
            if (y < cy) squares[2] else squares[3]
        }
    }

    companion object {
        fun newInstance() = DynamicPropertiesFragment()
    }
}