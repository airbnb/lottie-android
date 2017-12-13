package com.airbnb.lottie.animation.keyframe;

import android.support.annotation.FloatRange;

import com.airbnb.lottie.animation.Keyframe;

import java.util.Collections;

public class StaticKeyframeAnimation<K, A> extends BaseKeyframeAnimation<K, A> {
  private final A initialValue;

  public StaticKeyframeAnimation(A initialValue) {
    super(Collections.<Keyframe<K>>emptyList());
    this.initialValue = initialValue;
  }

  @Override public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    // Do nothing
  }

  @Override public void addUpdateListener(AnimationListener listener) {
    // Do nothing.
  }

  @Override public A getValue() {
    if (valueCallback != null) {
      return valueCallback.getValue(0f, 0f, initialValue, initialValue, 0f, 0f, 0f);
    }
    return initialValue;
  }

  @Override public A getValue(Keyframe<K> keyframe, float keyframeProgress) {
    return initialValue;
  }
}
