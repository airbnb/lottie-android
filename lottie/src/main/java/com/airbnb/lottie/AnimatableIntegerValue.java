package com.airbnb.lottie;

import org.json.JSONObject;

import java.util.List;

class AnimatableIntegerValue extends BaseAnimatableValue<Integer, Integer> {
  private AnimatableIntegerValue() {
    super(100);
  }

  AnimatableIntegerValue(List<Keyframe<Integer>> keyframes, Integer initialValue) {
    super(keyframes, initialValue);
  }

  @Override public KeyframeAnimation<Integer> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    }

    return new IntegerKeyframeAnimation(keyframes);
  }

  public Integer getInitialValue() {
    return initialValue;
  }

  static final class Factory {
    private Factory() {
    }

    static AnimatableIntegerValue newInstance() {
      return new AnimatableIntegerValue();
    }

    static AnimatableIntegerValue newInstance(
        JSONObject json, LottieComposition composition) {
      AnimatableValueParser.Result<Integer> result = AnimatableValueParser
          .newInstance(json, 1, composition, ValueFactory.INSTANCE)
          .parseJson();
      Integer initialValue = result.initialValue;
      return new AnimatableIntegerValue(result.keyframes, initialValue);
    }
  }

  private static class ValueFactory implements AnimatableValue.Factory<Integer> {
    private static final ValueFactory INSTANCE = new ValueFactory();

    private ValueFactory() {
    }

    @Override public Integer valueFromObject(Object object, float scale) {
      return Math.round(JsonUtils.valueFromObject(object) * scale);
    }
  }
}
