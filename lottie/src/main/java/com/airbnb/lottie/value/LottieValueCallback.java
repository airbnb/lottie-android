package com.airbnb.lottie.value;


import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;

/**
 * Allows you to set a callback on a resolved {@link com.airbnb.lottie.model.KeyPath} to modify
 * its animation values at runtime.
 */
public class LottieValueCallback<T> {
  private final LottieFrameInfo<T> frameInfo = new LottieFrameInfo<>();
  @Nullable private BaseKeyframeAnimation<?, ?> animation;

  /**
   * This can be set with {@link #setValue(Object)} to use a value instead of deferring
   * to the callback.
   **/
  @Nullable protected T value = null;

  public LottieValueCallback() {
  }

  public LottieValueCallback(@Nullable T staticValue) {
    value = staticValue;
  }

  /**
   * Override this if you haven't set a static value in the constructor or with setValue.
   *
   * Return null to resort to the default value.
   */
  @Nullable
  public T getValue(LottieFrameInfo<T> frameInfo) {
    return value;
  }

  public final void setValue(@Nullable T value) {
    this.value = value;
    if (animation != null) {
      animation.notifyListeners();
    }
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  @Nullable
  public final T getValueInternal(
      float startFrame,
      float endFrame,
      T startValue,
      T endValue,
      float linearKeyframeProgress,
      float interpolatedKeyframeProgress,
      float overallProgress
  ) {
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
