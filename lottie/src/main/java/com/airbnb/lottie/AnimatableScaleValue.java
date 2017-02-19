package com.airbnb.lottie;

import org.json.JSONArray;
import org.json.JSONObject;

class AnimatableScaleValue extends BaseAnimatableValue<ScaleXY, ScaleXY> {
  AnimatableScaleValue(LottieComposition composition) {
    super(composition);
    initialValue = new ScaleXY();
  }

  AnimatableScaleValue(JSONObject scaleValues, LottieComposition composition, boolean isDp) {
    super(scaleValues, composition, isDp);
  }

  @Override public ScaleXY valueFromObject(Object object, float scale) {
    JSONArray array = (JSONArray) object;
    return new ScaleXY().scale(
        (float) array.optDouble(0, 1) / 100f * scale,
        (float) array.optDouble(1, 1) / 100f * scale);
  }

  @Override public KeyframeAnimation<ScaleXY> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    }

    return new ScaleKeyframeAnimation(keyframes);
  }
}
