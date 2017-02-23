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

    static AnimatableIntegerValue newInstance(JSONObject json, LottieComposition composition,
        boolean isDp, boolean remap100To255) {
      float scale = isDp ? composition.getScale() : 1f;
      AnimatableValueParser.Result<Integer> result = AnimatableValueParser
          .newInstance(json, scale, composition, ValueFactory.INSTANCE)
          .parseJson();
      Integer initialValue = result.initialValue;
      if (remap100To255 && result.initialValue != null) {
        initialValue = result.initialValue * 255 / 100;
        int size = result.keyframes.size();
        for (int i = 0; i < size; i++) {
          Keyframe<Integer> keyframe = result.keyframes.get(i);
          keyframe.startValue = keyframe.startValue * 255 / 100;
          keyframe.endValue = keyframe.endValue * 255 / 100;
        }
      }
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
