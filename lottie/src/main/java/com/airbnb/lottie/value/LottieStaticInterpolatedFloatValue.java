package com.airbnb.lottie.value;

import android.view.animation.Interpolator;

import com.airbnb.lottie.utils.MiscUtils;

public class LottieStaticInterpolatedFloatValue extends LottieStaticInterpolatedValue<Float> {

  public LottieStaticInterpolatedFloatValue(Float startValue, Float endValue) {
    super(startValue, endValue);
  }

  public LottieStaticInterpolatedFloatValue(Float startValue, Float endValue,
      Interpolator interpolator) {
    super(startValue, endValue, interpolator);
  }

  @Override Float interpolateValue(Float startValue, Float endValue, float progress) {
    return MiscUtils.lerp(startValue, endValue, progress);
  }
}
