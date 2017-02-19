package com.airbnb.lottie;

interface AnimatableValue<V, O> {
  V valueFromObject(Object object, float scale);
  BaseKeyframeAnimation<?, O> createAnimation();
  boolean hasAnimation();
}
