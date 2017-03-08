package com.airbnb.lottie;

import org.json.JSONObject;

import java.util.List;

class AnimatableIntegerValue extends BaseAnimatableValue<Integer, Integer> {
  private AnimatableIntegerValue(LottieComposition composition, Integer initialValue) {
    super(composition, initialValue);
  }

  private AnimatableIntegerValue(List<Keyframe<Integer>> keyframes, LottieComposition composition,
      Integer initialValue) {
    super(keyframes, composition, initialValue);
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

    static AnimatableIntegerValue newInstance(LottieComposition composition, Integer initialValue) {
      return new AnimatableIntegerValue(composition, initialValue);
    }

    static AnimatableIntegerValue newInstance(
        JSONObject json, LottieComposition composition, boolean isDp) {
      float scale = isDp ? composition.getDpScale() : 1f;
      AnimatableValueParser.Result<Integer> result = AnimatableValueParser
          .newInstance(json, scale, composition, ValueFactory.INSTANCE)
          .parseJson();
      Integer initialValue = result.initialValue;
      return new AnimatableIntegerValue(result.keyframes, composition, initialValue);
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
