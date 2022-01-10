package com.airbnb.lottie.compose

import android.content.Context

object LottieConstants {
    /**
     * Use this with [animateLottieCompositionAsState]'s iterations parameter to repeat forever.
     */
    const val IterateForever = Integer.MAX_VALUE

    internal var SystemAnimationsDisabled = false

    internal fun updateSystemAnimationsDisabled(context: Context) {
        
    }
}