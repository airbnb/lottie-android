package com.airbnb.lottie;

interface AnimatableValue<T> {
  KeyframeAnimation<T> createAnimation();
  boolean hasAnimation();
}
