package com.airbnb.lottie.samples

import android.widget.SeekBar

internal open class OnSeekBarChangeListenerAdapter(
        val onProgressChanged: ((seekBar: SeekBar, progress: Int, fromUser: Boolean) -> Unit)? = null,
        val onStartTrackingTouch: ((seekBar: SeekBar) -> Unit)? = null,
        val onStopTrackingTouch: ((seekBar: SeekBar) -> Unit)? = null
): SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) =
        onProgressChanged?.invoke(seekBar, progress, fromUser) ?: Unit

    override fun onStartTrackingTouch(seekBar: SeekBar) =
            onStartTrackingTouch?.invoke(seekBar) ?: Unit

    override fun onStopTrackingTouch(seekBar: SeekBar) =
            onStopTrackingTouch?.invoke(seekBar) ?: Unit
}
