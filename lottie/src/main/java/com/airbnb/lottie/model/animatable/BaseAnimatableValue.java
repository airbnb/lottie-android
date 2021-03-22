package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.value.Keyframe;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

abstract class BaseAnimatableValue<V, O> implements AnimatableValue<V, O> {
  final List<Keyframe<V>> keyframes;

  /**
   * Create a default static animatable path.
   */
  BaseAnimatableValue(V value) {
    this(Collections.singletonList(new Keyframe<>(value)));
  }

  BaseAnimatableValue(List<Keyframe<V>> keyframes) {
    this.keyframes = keyframes;
  }

  public List<Keyframe<V>> getKeyframes() {
    return keyframes;
  }

  @Override
  public boolean isStatic() {
    return keyframes.isEmpty() || (keyframes.size() == 1 && keyframes.get(0).isStatic());
  }

  @Override public String toString() {
    final StringBuilder sb = new StringBuilder();
    if (!keyframes.isEmpty()) {
      sb.append("values=").append(Arrays.toString(keyframes.toArray()));
    }
    return sb.toString();
  }
}
