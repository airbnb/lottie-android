package com.airbnb.lottie.value;


import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;

/**
 * Allows you to set a callback on a resolved {@link com.airbnb.lottie.model.KeyPath} to modify
 * its animation values at runtime.
 *
 * If your dynamic property does the following, you must call {@link LottieAnimationView#invalidate()} or
 * {@link LottieDrawable#invalidateSelf()} each time you want to update this value.
 * 1. Use {@link com.airbnb.lottie.RenderMode.SOFTWARE}
 * 2. Rendering a static image (the animation is either paused or there are no values
 *    changing within the animation itself)
 * When using software rendering, Lottie caches the internal rendering bitmap. Whenever the animation changes
 * internally, Lottie knows to invalidate the bitmap and re-render it on the next frame. If the animation
 * never changes but your dynamic property does outside of Lottie, Lottie must be notified that it changed
 * in order to set the bitmap as dirty and re-render it on the next frame.
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
   * <p>
   * Return null to resort to the default value.
   *
   * Refer to the javadoc for this class for a special case that requires manual invalidation
   * each time you want to return something different from this method.
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
