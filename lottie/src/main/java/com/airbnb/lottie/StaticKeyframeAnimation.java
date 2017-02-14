package com.airbnb.lottie;

import android.support.annotation.FloatRange;

import java.util.Collections;

class StaticKeyframeAnimation<T> extends KeyframeAnimation<T> {
  private final T initialValue;

  StaticKeyframeAnimation(T initialValue) {
    super(Collections.<Keyframe<T>>emptyList());
    this.initialValue = initialValue;
  }

  @Override public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    // Do nothing
  }

  @Override public T getValue() {
    return initialValue;
  }

  @Override public T getValue(Keyframe<T> keyframe, float keyframeProgress) {
    return initialValue;
  }
}
