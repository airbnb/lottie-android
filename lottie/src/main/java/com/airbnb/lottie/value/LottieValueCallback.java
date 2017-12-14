package com.airbnb.lottie.value;


import android.support.annotation.RestrictTo;

/**
 * Allows you to set a callback on a resolved {@link com.airbnb.lottie.model.KeyPath} to modify
 * its animation values at runtime.
 *
 * This API is not ready for public use yet.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface LottieValueCallback<T> {
  T getValue(
      float startFrame, float endFrame,
      T startValue, T endValue,
      float linearKeyframeProgress, float interpolatedKeyframeProgress,
      float overallProgress);
}
