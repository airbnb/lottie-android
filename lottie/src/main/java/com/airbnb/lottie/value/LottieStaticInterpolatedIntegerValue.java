package com.airbnb.lottie.value;

import android.view.animation.Interpolator;

import com.airbnb.lottie.utils.MiscUtils;

public class LottieStaticInterpolatedIntegerValue extends LottieStaticInterpolatedValue<Integer> {

  public LottieStaticInterpolatedIntegerValue(Integer startValue, Integer endValue) {
    super(startValue, endValue);
  }

  public LottieStaticInterpolatedIntegerValue(Integer startValue, Integer endValue,
      Interpolator interpolator) {
    super(startValue, endValue, interpolator);
  }

  @Override Integer interpolateValue(Integer startValue, Integer endValue, float progress) {
    return MiscUtils.lerp(startValue, endValue, progress);
  }
}
