package com.airbnb.lottie.value;

public class LottieStaticValueCallback<T> implements LottieValueCallback<T> {

  private final T value;

  public LottieStaticValueCallback(T value) {
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
