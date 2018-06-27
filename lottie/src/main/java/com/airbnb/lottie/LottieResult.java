package com.airbnb.lottie;

import android.support.annotation.Nullable;

/**
 * TODO
 */
public class LottieResult<V> {

  @Nullable private final V result;
  @Nullable private final Throwable error;

  public LottieResult(V result) {
    this.result = result;
    error = null;
  }

  public LottieResult(Throwable error) {
    this.error = error;
    result = null;
  }
}
