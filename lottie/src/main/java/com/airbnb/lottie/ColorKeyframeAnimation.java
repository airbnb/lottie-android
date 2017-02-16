package com.airbnb.lottie;

import android.animation.ArgbEvaluator;

import java.util.List;

class ColorKeyframeAnimation extends KeyframeAnimation<Integer> {
  private final ArgbEvaluator argbEvaluator = new ArgbEvaluator();

  ColorKeyframeAnimation(List<Keyframe<Integer>> keyframes) {
    super(keyframes);
  }

  @Override public Integer getValue(Keyframe<Integer> keyframe, float keyframeProgress) {
    int startColor = keyframe.startValue;
    int endColor = keyframe.endValue;

    return (Integer) argbEvaluator.evaluate(keyframeProgress, startColor, endColor);
  }
}
