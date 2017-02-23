package com.airbnb.lottie;

import org.json.JSONObject;

class AnimatablePointValue extends BaseAnimatableValue<CPointF, CPointF> {
  AnimatablePointValue(JSONObject pointValues, LottieComposition composition) {
    super(pointValues, composition, true);
  }

  @Override public CPointF valueFromObject(Object object, float scale) {
    return PointFFactory.newInstance(object, scale);
  }

  @Override public KeyframeAnimation<CPointF> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    }

    return new PointKeyframeAnimation(keyframes);
  }
}
