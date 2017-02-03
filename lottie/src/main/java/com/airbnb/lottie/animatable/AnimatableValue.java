package com.airbnb.lottie.animatable;

import com.airbnb.lottie.animation.KeyframeAnimation;

interface AnimatableValue {

  KeyframeAnimation createAnimation();

  boolean hasAnimation();
}
