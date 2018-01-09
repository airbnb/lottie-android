package com.airbnb.lottie.value;

/**
 * Static value version of {@link LottieRelativeFloatValueCallback}.
 */
public class LottieStaticRelativeFloatValue extends LottieRelativeFloatValueCallback {

  private final float offset;

  public LottieStaticRelativeFloatValue(float offset) {
    this.offset = offset;
  }

  @Override
  public Float getOffset(
      float startFrame, float endFrame,
      Float startValue, Float endValue,
      float linearKeyframeProgress, float interpolatedKeyframeProgress,
      float overallProgress) {
    return offset;
  }
}
