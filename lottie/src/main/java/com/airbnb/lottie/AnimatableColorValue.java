package com.airbnb.lottie;

import org.json.JSONObject;

class AnimatableColorValue extends BaseAnimatableValue<Integer, Integer> {
  AnimatableColorValue(JSONObject json, LottieComposition composition) {
    super(json, composition, false);
  }

  @Override public Integer valueFromObject(Object object, float scale) {
    return ColorFactory.newInstance(object);
  }

  @Override public KeyframeAnimation<Integer> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    }
    return new ColorKeyframeAnimation(keyframes);
  }

  @Override public String toString() {
    return "AnimatableColorValue{" + "initialValue=" + initialValue + '}';
  }
}
