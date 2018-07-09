package com.airbnb.lottie;

import android.support.annotation.Nullable;

import java.util.Objects;

/**
 * TODO
 */
public class LottieResult<V> {

  @Nullable private final V value;
  @Nullable private final Throwable exception;

  public LottieResult(V value) {
    this.value = value;
    exception = null;
  }

  public LottieResult(Throwable exception) {
    this.exception = exception;
    value = null;
  }

  @Nullable public V getValue() {
    return value;
  }

  @Nullable public Throwable getException() {
    return exception;
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LottieResult)) {
      return false;
    }
    LottieResult<?> that = (LottieResult<?>) o;
    if (Objects.equals(getValue(), that.getValue())) {
      return true;
    }
    if (getException() != null && that.getException() != null) {
      return Objects.equals(getException().toString(), that.getException().toString());
    }
    return false;
  }

  @Override public int hashCode() {

    return Objects.hash(getValue(), getException());
  }
}
