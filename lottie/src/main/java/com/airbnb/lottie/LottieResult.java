package com.airbnb.lottie;

import androidx.annotation.Nullable;

import java.util.Arrays;

/**
 * Contains class to hold the resulting value of an async task or an exception if it failed.
 *
 * Either value or exception will be non-null.
 */
public final class LottieResult<V> {

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
    if (getValue() != null && getValue().equals(that.getValue())) {
      return true;
    }
    if (getException() != null && that.getException() != null) {
      return getException().toString().equals(getException().toString());
    }
    return false;
  }

  @Override public int hashCode() {
    return Arrays.hashCode(new Object[]{getValue(), getException()});
  }
}
