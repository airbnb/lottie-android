package com.airbnb.lottie.samples

import androidx.viewpager.widget.ViewPager

internal open class OnPageChangeListenerAdapter(
        private val onPageScrollStateChanged: ((state: Int) -> Unit)? = null,
        private val onPageScrolled:
                ((position: Int, positionOffset: Float, positionOffsetPixels: Int) -> Unit)? = null,
        private val onPageSelected: ((position: Int) -> Unit)? = null
): ViewPager.OnPageChangeListener {
    override fun onPageScrollStateChanged(state: Int) =
            onPageScrollStateChanged?.invoke(state) ?: Unit

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) =
            onPageScrolled?.invoke(position, positionOffset, positionOffsetPixels) ?: Unit

    override fun onPageSelected(position: Int) = onPageSelected?.invoke(position) ?: Unit
}