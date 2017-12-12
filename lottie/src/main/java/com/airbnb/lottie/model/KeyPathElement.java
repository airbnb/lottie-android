package com.airbnb.lottie.model;

import android.support.annotation.IntDef;

import com.airbnb.lottie.LottieValueCallback;

import java.util.List;

/**
 * Any item that can be a part of a {@link KeyPath} should implement this.
 */

public interface KeyPathElement {

  @IntDef({ COLOR })
  public @interface Property {}
  public static final int COLOR = 0;


  void resolveKeyPath(KeyPath keyPath, int depth, List<KeyPath> accumulator,
      KeyPath currentPartialKeyPath);

  <T> void applyValueCallback(@Property int property, LottieValueCallback<T> callback);
}