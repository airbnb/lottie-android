package com.airbnb.lottie.samples

import android.util.Log
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionListener
import com.airbnb.lottie.model.KeyPath

class KeyPathsHierarchyLogger(val animationView: LottieAnimationView) : LottieCompositionListener {

    companion object {
        val TAG = KeyPathsHierarchyLogger::class.simpleName
    }

    override fun onCompositionLoaded() {
        animationView.resolveKeyPath(KeyPath("**")).forEach {
            Log.d(TAG, it.keysToString())
        }
    }

}