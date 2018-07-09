package com.airbnb.lottie;

/**
 * Receive a result with either the value or exception for a {@link LottieTask}
 */
public interface LottieTaskListener<T> {
  void onResult(LottieResult<T> result);
}
