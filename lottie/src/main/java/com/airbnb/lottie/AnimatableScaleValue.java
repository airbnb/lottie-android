package com.airbnb.lottie;

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
    return ScaleXY.Factory.newInstance(object, scale);
  }

  @Override public KeyframeAnimation<ScaleXY> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    } else {
      return new ScaleKeyframeAnimation(keyframes);
    }
  }
}
