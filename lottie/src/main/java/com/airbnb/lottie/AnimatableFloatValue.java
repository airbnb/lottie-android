package com.airbnb.lottie;

import org.json.JSONObject;

import java.util.List;

class AnimatableFloatValue extends BaseAnimatableValue<Float, Float> {
  private AnimatableFloatValue(LottieComposition composition, Float initialValue) {
    super(composition, initialValue);
  }

  private AnimatableFloatValue(List<Keyframe<Float>> keyframes,
      LottieComposition composition, Float initialValue) {
    super(keyframes, composition, initialValue);
  }

  @Override public KeyframeAnimation<Float> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    }

    return new FloatKeyframeAnimation(keyframes);
  }

  public Float getInitialValue() {
    return initialValue;
  }

  private static class ValueFactory implements AnimatableValue.Factory<Float> {
    static final ValueFactory INSTANCE = new ValueFactory();

    private ValueFactory() {
    }

    @Override public Float valueFromObject(Object object, float scale) {
      return JsonUtils.valueFromObject(object) * scale;
    }
  }

  static final class Factory {
    private Factory() {
    }

    static AnimatableFloatValue newInstance(LottieComposition composition, Float initialValue) {
      return new AnimatableFloatValue(composition, initialValue);
    }

    static AnimatableFloatValue newInstance(JSONObject json, LottieComposition composition) {
      return newInstance(json, composition, true);
    }

    static AnimatableFloatValue newInstance(JSONObject json, LottieComposition composition,
        boolean isDp) {
      float scale = isDp ? composition.getDpScale() : 1f;
      AnimatableValueParser.Result<Float> result = AnimatableValueParser
          .newInstance(json, scale, composition, ValueFactory.INSTANCE)
          .parseJson();
      return new AnimatableFloatValue(result.keyframes, composition, result.initialValue);
    }
  }
}
