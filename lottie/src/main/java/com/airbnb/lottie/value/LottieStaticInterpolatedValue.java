package com.airbnb.lottie.value;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

abstract class LottieStaticInterpolatedValue<T> extends LottieValueCallback<T> {

  private final T startValue;
  private final T endValue;
  private final Interpolator interpolator;

  LottieStaticInterpolatedValue(T startValue, T endValue) {
    this(startValue, endValue, new LinearInterpolator());
  }

  LottieStaticInterpolatedValue(T startValue, T endValue, Interpolator interpolator) {
    this.startValue = startValue;
    this.endValue = endValue;
    this.interpolator = interpolator;
  }

  @Override public T getValue(float startFrame, float endFrame, T startValue, T endValue,
      float linearKeyframeProgress, float interpolatedKeyframeProgress, float overallProgress) {
    float progress = interpolator.getInterpolation(overallProgress);
    return interpolateValue(this.startValue, this.endValue, progress);
  }

  abstract T interpolateValue(T startValue, T endValue, float progress);
}
