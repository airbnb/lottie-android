package com.airbnb.lottie.samples

import android.widget.SeekBar

internal class OnSeekBarChangeListenerAdapter(
        private val onProgressChanged: ((seekBar: SeekBar, progress: Int, fromUser: Boolean) -> Unit)? = null,
        private val onStartTrackingTouch: ((seekBar: SeekBar) -> Unit)? = null,
        private val onStopTrackingTouch: ((seekBar: SeekBar) -> Unit)? = null
) : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) =
            onProgressChanged?.invoke(seekBar, progress, fromUser) ?: Unit

    override fun onStartTrackingTouch(seekBar: SeekBar) =
            onStartTrackingTouch?.invoke(seekBar) ?: Unit

    override fun onStopTrackingTouch(seekBar: SeekBar) =
            onStopTrackingTouch?.invoke(seekBar) ?: Unit
}
