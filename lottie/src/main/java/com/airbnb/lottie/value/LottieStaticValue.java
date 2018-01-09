package com.airbnb.lottie.value;

public class LottieStaticValue<T> extends LottieValueCallback<T> {

  private final T value;

  public LottieStaticValue(T value) {
    this.value = value;
  }

  @Override public T getValue(
      float startFrame, float endFrame,
      T startValue, T endValue,
      float linearKeyframeProgress, float interpolatedKeyframeProgress,
      float overallProgress) {
    return value;
  }
}
