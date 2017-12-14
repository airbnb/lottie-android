package com.airbnb.lottie.value;

import com.airbnb.lottie.utils.MiscUtils;

/**
 * {@link LottieValueCallback} that provides a value offset from the original animation
 * rather than an absolute value.
 */
public abstract class LottieIntegerRelativeValueCallback implements LottieValueCallback<Integer> {
  @Override
  public Integer getValue(float startFrame, float endFrame, Integer startValue, Integer endValue,
      float linearKeyframeProgress, float interpolatedKeyframeProgress, float overallProgress) {
    int originalValue = MiscUtils.lerp(startValue, endValue, interpolatedKeyframeProgress);
    int newValue = getOffset(startFrame, endFrame, startValue, endValue,
        linearKeyframeProgress, interpolatedKeyframeProgress, overallProgress);
    return originalValue + newValue;
  }

  public abstract Integer getOffset(
      float startFrame, float endFrame,
      Integer startValue, Integer endValue,
      float linearKeyframeProgress, float interpolatedKeyframeProgress, float overallProgress);
}
