package com.airbnb.lottie.samples

import android.support.v4.util.Pair

internal interface ILottieApplication {
    fun startRecordingDroppedFrames()

    /**
     * Returns the number of frames dropped since starting
     */
    fun stopRecordingDroppedFrames(): Pair<Int, Long>
}
