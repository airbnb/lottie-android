package com.airbnb.lottie;

import android.util.Log;

import org.json.JSONObject;

import java.util.List;

class AnimatableFloatValue extends BaseAnimatableValue<Float, Float> {
  private AnimatableFloatValue() {
    super(0f);
  }

  private AnimatableFloatValue(List<Keyframe<Float>> keyframes, Float initialValue) {
    super(keyframes, initialValue);
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

    static AnimatableFloatValue newInstance() {
      return new AnimatableFloatValue();
    }

    static AnimatableFloatValue newInstance(JSONObject json, LottieComposition composition) {
      return newInstance(json, composition, true);
    }

    static AnimatableFloatValue newInstance(JSONObject json, LottieComposition composition,
        boolean isDp) {
      float scale = isDp ? composition.getDpScale() : 1f;
      if (json.has("x")) {
        Log.w(L.TAG, "Animation has expressions which are not supported.");
      }
      AnimatableValueParser.Result<Float> result = AnimatableValueParser
          .newInstance(json, scale, composition, ValueFactory.INSTANCE)
          .parseJson();
      return new AnimatableFloatValue(result.keyframes, result.initialValue);
    }
  }
}
