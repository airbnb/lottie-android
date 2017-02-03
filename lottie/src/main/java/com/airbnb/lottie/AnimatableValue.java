package com.airbnb.lottie;

interface AnimatableValue {
  KeyframeAnimation createAnimation();
  boolean hasAnimation();
}
