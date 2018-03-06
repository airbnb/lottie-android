package com.airbnb.lottie.samples.views

import android.annotation.SuppressLint
import android.content.Context
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.view.children

private val TAG = NoShiftBottomNavigationView::class.java.name

class NoShiftBottomNavigationView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : BottomNavigationView(context, attrs, defStyleAttr) {

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        removeShiftAnimation()
    }

    @SuppressLint("RestrictedApi")
    private fun removeShiftAnimation() {
        val menuView = getChildAt(0) as BottomNavigationMenuView
        try {
            menuView::class.java.getDeclaredField("mShiftingMode").apply {
                isAccessible = true
                setBoolean(menuView, false)
                isAccessible = false
            }

            menuView.children
                    .map { it as BottomNavigationItemView }
                    .forEach {
                        it.setShiftingMode(false)
                        it.setChecked(it.itemData.isChecked)
                    }
        } catch (e: NoSuchFieldException) {
            Log.e(TAG, "Unable to get shift mode field", e)
        } catch (e: IllegalAccessException) {
            Log.e(TAG, "Unable to change value of shift mode", e)
        }
    }
}