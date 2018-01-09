package com.airbnb.lottie.value;


import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;

/**
 * Allows you to set a callback on a resolved {@link com.airbnb.lottie.model.KeyPath} to modify
 * its animation values at runtime.
 *
 * This API is not ready for public use yet.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public abstract class LottieValueCallback<T> {
  @Nullable BaseKeyframeAnimation<?, ?> animation;

  /**
   * This can be set with {@link #setValue(Object)} to use a value instead of deferring
   * to the callback.
   * */
  @Nullable private T value = null;

  public abstract T getValue(
      float startFrame,
      float endFrame,
      T startValue,
      T endValue,
      float linearKeyframeProgress,
      float interpolatedKeyframeProgress,
      float overallProgress);

  public void setValue(@Nullable T value) {
    if (animation != null) {
      this.value = value;
      animation.notifyListeners();
    }
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public T getValueInternal(
      float startFrame,
      float endFrame,
      T startValue,
      T endValue,
      float linearKeyframeProgress,
      float interpolatedKeyframeProgress,
      float overallProgress
  ) {
    if (value != null) {
      return value;
    }
    return getValue(startFrame, endFrame, startValue, endValue, linearKeyframeProgress,
        interpolatedKeyframeProgress, overallProgress);
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public void setAnimation(@Nullable BaseKeyframeAnimation<?, ?> animation) {
    this.animation = animation;
  }
}
