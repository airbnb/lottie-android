package com.airbnb.lottie;

import android.graphics.PointF;

import org.json.JSONObject;

class AnimatablePointValue extends BaseAnimatableValue<PointF, PointF> {
  AnimatablePointValue(JSONObject pointValues, LottieComposition composition) {
    super(pointValues, composition, true);
  }

  @Override public PointF valueFromObject(Object object, float scale) {
    return PointFFactory.newInstance(object, scale);
  }

  @Override public KeyframeAnimation<PointF> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    }

    return new PointKeyframeAnimation(keyframes);
  }
}
