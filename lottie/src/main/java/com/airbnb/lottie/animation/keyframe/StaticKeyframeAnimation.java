package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.animation.Keyframe;

import java.util.Collections;

public class StaticKeyframeAnimation<K, A> extends BaseKeyframeAnimation<K, A> {
  private final A initialValue;

  public StaticKeyframeAnimation(A initialValue) {
    super(Collections.<Keyframe<K>>emptyList());
    this.initialValue = initialValue;
  }

  @Override public void setProgress(float progress) {
    super.setProgress(progress);
  }

  /**
   * If this doesn't return 1, then {@link #setProgress(float)} will always clamp the progress
   * to 0.
   */
  @Override float getEndProgress() {
    return 1f;
  }

  @Override void notifyListeners() {
    if (this.valueCallback != null) {
      super.notifyListeners();
    }
  }

  @Override public A getValue() {
    if (valueCallback != null) {
      return valueCallback.getValue(
          0f, 0f,
          initialValue, initialValue,
          getProgress(), getProgress(), getProgress());
    }
    return initialValue;
  }

  @Override public A getValue(Keyframe<K> keyframe, float keyframeProgress) {
    return initialValue;
  }
}
