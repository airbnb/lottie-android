package com.airbnb.lottie;

public interface LottieTaskListener<T> {
  void onResult(LottieResult<T> result);
}
