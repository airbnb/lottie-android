package com.airbnb.lottie.value;

import com.airbnb.lottie.utils.MiscUtils;

/**
 * {@link LottieValueCallback} that provides a value offset from the original animation
 * rather than an absolute value.
 */
public abstract class LottieRelativeFloatValueCallback extends LottieValueCallback<Float> {
  @Override
  public Float getValue(float sf, float ef, Float sv, Float ev, float lkp, float ikp, float p) {
    float originalValue = MiscUtils.lerp(sv, ev, ikp);
    float offset = getOffset(sf, ef, sv, ev, lkp, ikp, p);
    return originalValue + offset;
  }

  public abstract Float getOffset(
      float startFrame, float endFrame,
      Float startValue, Float endValue,
      float linearKeyframeProgress, float interpolatedKeyframeProgress,
      float overallProgress);
}
