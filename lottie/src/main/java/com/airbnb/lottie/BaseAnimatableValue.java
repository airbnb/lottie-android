package com.airbnb.lottie;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

abstract class BaseAnimatableValue<V, O> implements AnimatableValue<V, O> {
  final List<Keyframe<V>> keyframes;
  final LottieComposition composition;
  V initialValue;

  /**
   * Create a default static animatable path.
   */
  BaseAnimatableValue(LottieComposition composition) {
    this.composition = composition;
    this.keyframes = Collections.emptyList();
  }

  BaseAnimatableValue(List<Keyframe<V>> keyframes, LottieComposition composition, V initialValue) {
    this.keyframes = keyframes;
    this.composition = composition;
    this.initialValue = initialValue;
  }

  /**
   * Convert the value type of the keyframe to the value type of the animation. Often, these
   * are the same type.
   */
  O convertType(V value) {
    //noinspection unchecked
    return (O) value;
  }

  public boolean hasAnimation() {
    return !keyframes.isEmpty();
  }

  public O getInitialValue() {
    return convertType(initialValue);
  }

  public abstract BaseKeyframeAnimation<?, O> createAnimation();

  @Override public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("parseInitialValue=").append(initialValue);
    if (!keyframes.isEmpty()) {
      sb.append(", values=").append(Arrays.toString(keyframes.toArray()));
    }
    return sb.toString();
  }
}
