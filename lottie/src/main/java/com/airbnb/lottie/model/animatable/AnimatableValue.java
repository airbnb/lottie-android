package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;

public interface AnimatableValue<O> {
  BaseKeyframeAnimation<?, O> createAnimation();
  boolean hasAnimation();

  interface Factory<V> {
    V valueFromObject(Object object, float scale);
  }
}
