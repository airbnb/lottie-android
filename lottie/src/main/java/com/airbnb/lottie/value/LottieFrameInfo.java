package com.airbnb.lottie.value;

import androidx.annotation.RestrictTo;

/**
 * Data class for use with {@link LottieValueCallback}.
 * You should *not* hold a reference to the frame info parameter passed to your callback. It will be reused.
 */
public class LottieFrameInfo<T> {
  private float startFrame;
  private float endFrame;
  private T startValue;
  private T endValue;
  private float linearKeyframeProgress;
  private float interpolatedKeyframeProgress;
  private float overallProgress;

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public LottieFrameInfo<T> set(
      float startFrame,
      float endFrame,
      T startValue,
      T endValue,
      float linearKeyframeProgress,
      float interpolatedKeyframeProgress,
      float overallProgress
  ) {
    this.startFrame = startFrame;
    this.endFrame = endFrame;
    this.startValue = startValue;
    this.endValue = endValue;
    this.linearKeyframeProgress = linearKeyframeProgress;
    this.interpolatedKeyframeProgress = interpolatedKeyframeProgress;
    this.overallProgress = overallProgress;
    return this;
  }

  public float getStartFrame() {
    return startFrame;
  }

  public float getEndFrame() {
    return endFrame;
  }

  public T getStartValue() {
    return startValue;
  }

  public T getEndValue() {
    return endValue;
  }

  public float getLinearKeyframeProgress() {
    return linearKeyframeProgress;
  }

  public float getInterpolatedKeyframeProgress() {
    return interpolatedKeyframeProgress;
  }

  public float getOverallProgress() {
    return overallProgress;
  }
}
