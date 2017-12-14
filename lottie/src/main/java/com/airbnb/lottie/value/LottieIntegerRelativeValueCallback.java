package com.airbnb.lottie.value;

import com.airbnb.lottie.utils.MiscUtils;

/**
 * {@link LottieValueCallback} that provides a value offset from the original animation
 * rather than an absolute value.
 */
public abstract class LottieIntegerRelativeValueCallback implements LottieValueCallback<Integer> {
  @Override
  public Integer getValue(
      float sf, float ef, Integer sv, Integer ev, float lkp, float ikp, float p) {
    int originalValue = MiscUtils.lerp(sv, ev, ikp);
    int newValue = getOffset(sf, ef, sv, ev, lkp, ikp, p);
    return originalValue + newValue;
  }

  public abstract Integer getOffset(
      float startFrame, float endFrame,
      Integer startValue, Integer endValue,
      float linearKeyframeProgress, float interpolatedKeyframeProgress,
      float overallProgress);
}
