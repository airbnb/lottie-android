package com.airbnb.lottie.animation.keyframe;

import android.support.annotation.FloatRange;

import com.airbnb.lottie.animation.Keyframe;

import java.util.Collections;

public class StaticKeyframeAnimation<A> extends BaseKeyframeAnimation<A, A> {
  private A initialValue;

  public StaticKeyframeAnimation(A initialValue) {
    super(Collections.<Keyframe<A>>emptyList());
    this.initialValue = initialValue;
  }

  @Override public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    // Do nothing
  }

  @Override public void addUpdateListener(AnimationListener listener) {
    // Do nothing.
  }

  @Override public A getValue() {
    return initialValue;
  }

  @Override public A getValue(Keyframe<A> keyframe, float keyframeProgress) {
    return initialValue;
  }

  /**
   * This can be used to dynamically update an animation at runtime.
   */
  @Override public void setValue(A value, int frame, boolean updateValue) {
    if (!value.equals(initialValue)) {
      initialValue = value;
      notifyListeners();
    }
  }
}
