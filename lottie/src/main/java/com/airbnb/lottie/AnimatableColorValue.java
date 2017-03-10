package com.airbnb.lottie;

import org.json.JSONObject;

import java.util.List;

class AnimatableColorValue extends BaseAnimatableValue<Integer, Integer> {
  private AnimatableColorValue(List<Keyframe<Integer>> keyframes, Integer initialValue) {
    super(keyframes, initialValue);
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

  static final class Factory {
    private Factory() {
    }

    static AnimatableColorValue newInstance(JSONObject json, LottieComposition composition) {
      AnimatableValueParser.Result<Integer> result = AnimatableValueParser
          .newInstance(json, 1f, composition, ColorFactory.INSTANCE)
          .parseJson();
      return new AnimatableColorValue(result.keyframes, result.initialValue);
    }
  }
}
