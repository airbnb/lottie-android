package com.airbnb.lottie;

interface AnimatableValue<O> {
  BaseKeyframeAnimation<?, O> createAnimation();
  boolean hasAnimation();

  interface Factory<V> {
    V valueFromObject(Object object, float scale);
  }
}
