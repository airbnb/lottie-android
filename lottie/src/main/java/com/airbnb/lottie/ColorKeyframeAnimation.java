package com.airbnb.lottie;

import android.animation.ArgbEvaluator;
import android.view.animation.Interpolator;

import java.util.List;

class ColorKeyframeAnimation extends KeyframeAnimation<Integer> {
  private final ArgbEvaluator argbEvaluator = new ArgbEvaluator();
  private final List<Integer> values;

  ColorKeyframeAnimation(long duration, LottieComposition composition, List<Float> keyTimes,
      List<Integer> values, List<Interpolator> interpolators) {
    super(duration, composition, keyTimes, interpolators);
    if (keyTimes.size() != values.size()) {
      throw new IllegalArgumentException(
          "Key times and values must be the same length " + keyTimes.size() + " vs " +
              values.size());
    }
    this.values = values;
  }

  @Override public Integer getValue() {
    if (progress <= 0f) {
      return values.get(0);
    } else if (progress >= 1f) {
      return values.get(values.size() - 1);
    }

    int keyframeIndex = getKeyframeIndex();

    float startKeytime = keyTimes.get(keyframeIndex);
    float endKeytime = keyTimes.get(keyframeIndex + 1);

    float percentageIntoFrame = 0;
    if (!isDiscrete) {
      percentageIntoFrame = (progress - startKeytime) / (endKeytime - startKeytime);
      if (interpolators != null) {
        percentageIntoFrame =
            interpolators.get(keyframeIndex).getInterpolation(percentageIntoFrame);
      }
    }

    int startColor = values.get(keyframeIndex);
    int endColor = values.get(keyframeIndex + 1);

    return (Integer) argbEvaluator.evaluate(percentageIntoFrame, startColor, endColor);
  }
}
