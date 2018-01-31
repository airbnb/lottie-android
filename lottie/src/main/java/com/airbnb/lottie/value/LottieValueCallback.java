package com.airbnb.lottie.value;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;

/**
 * Allows you to set a callback on a resolved {@link com.airbnb.lottie.model.KeyPath} to modify
 * its animation values at runtime.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class LottieValueCallback<T> {
  private final LottieFrameInfo<T> frameInfo = new LottieFrameInfo<>();
  @Nullable BaseKeyframeAnimation<?, ?> animation;

  /**
   * This can be set with {@link #setValue(Object)} to use a value instead of deferring
   * to the callback.
   **/
  @Nullable protected T value = null;

  public LottieValueCallback() {
  }

  public LottieValueCallback(@NonNull T staticValue) {
    value = staticValue;
  }

  /**
   * Override this if you haven't set a static value in the constructor or with setValue.
   */
  public T getValue(LottieFrameInfo<T> frameInfo) {
    if (value == null) {
      throw new IllegalArgumentException("You must provide a static value in the constructor " +
          ", call setValue, or override getValue.");
    }
    return value;
  }

  public final void setValue(@Nullable T value) {
    if (animation != null) {
      this.value = value;
      animation.notifyListeners();
    }
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public final T getValueInternal(
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
    return getValue(
        frameInfo.set(
            startFrame,
            endFrame,
            startValue,
            endValue,
            linearKeyframeProgress,
            interpolatedKeyframeProgress,
            overallProgress
        )
    );
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public final void setAnimation(@Nullable BaseKeyframeAnimation<?, ?> animation) {
    this.animation = animation;
  }
}
