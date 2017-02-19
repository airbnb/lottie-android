package com.airbnb.lottie;

import org.json.JSONObject;

class AnimatableFloatValue extends BaseAnimatableValue<Float, Float> {
  AnimatableFloatValue(LottieComposition composition, Float initialValue) {
    super(composition);
    this.initialValue = initialValue;
  }

  AnimatableFloatValue(JSONObject json, LottieComposition composition) {
    this(json, composition, true);
  }

  AnimatableFloatValue(JSONObject json, LottieComposition composition, boolean isDp) {
    super(json, composition, isDp);
  }

  @Override public Float valueFromObject(Object object, float scale) {
    return JsonUtils.valueFromObject(object) * scale;
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
}
