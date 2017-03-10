package com.airbnb.lottie;

import java.util.List;

class ColorKeyframeAnimation extends KeyframeAnimation<Integer> {

  ColorKeyframeAnimation(List<Keyframe<Integer>> keyframes) {
    super(keyframes);
  }

  @Override public Integer getValue(Keyframe<Integer> keyframe, float keyframeProgress) {
    if (keyframe.startValue == null || keyframe.endValue == null) {
      throw new IllegalStateException("Missing values for keyframe.");
    }
    int startColor = keyframe.startValue;
    int endColor = keyframe.endValue;

    return GammaEvaluator.evaluate(keyframeProgress, startColor, endColor);
  }
}
