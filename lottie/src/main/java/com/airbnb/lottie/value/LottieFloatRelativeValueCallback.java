package com.airbnb.lottie.value;

import com.airbnb.lottie.utils.MiscUtils;

/**
 * {@link LottieValueCallback} that provides a value offset from the original animation
 * rather than an absolute value.
 */
public abstract class LottieFloatRelativeValueCallback implements LottieValueCallback<Float> {
  @Override
  public Float getValue(float startFrame, float endFrame, Float startValue, Float endValue,
      float linearKeyframeProgress, float interpolatedKeyframeProgress, float overallProgress) {
    float originalValue = MiscUtils.lerp(startValue, endValue, interpolatedKeyframeProgress);
    float offset = getOffset(startFrame, endFrame, startValue, endValue,
        linearKeyframeProgress, interpolatedKeyframeProgress, overallProgress);
    return originalValue + offset;
  }

  public abstract Float getOffset(
      float startFrame, float endFrame,
      Float startValue, Float endValue,
      float linearKeyframeProgress, float interpolatedKeyframeProgress, float overallProgress);
}
