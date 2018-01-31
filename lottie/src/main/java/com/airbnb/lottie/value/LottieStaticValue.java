package com.airbnb.lottie.value;

public class LottieStaticValue<T> extends LottieValueCallback<T> {

  private final T value;

  public LottieStaticValue(T value) {
    this.value = value;
  }

  @Override public T getValue(LottieFrameInfo<T> frameInfo) {
    return value;
  }
}
