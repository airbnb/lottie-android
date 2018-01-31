package com.airbnb.lottie.value;

public interface SimpleLottieValueCallback<T> {
  T getValue(LottieFrameInfo<T> frameInfo);
}
