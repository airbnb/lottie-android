package com.airbnb.lottie.value;

/**
 * Static value version of {@link LottieFloatRelativeValueCallback}.
 */
public class LottieStaticFloatRelativeValueCallback extends LottieFloatRelativeValueCallback {

  private final float offset;

  public LottieStaticFloatRelativeValueCallback(float offset) {
    this.offset = offset;
  }

  @Override
  public Float getOffset(float startFrame, float endFrame, Float startValue, Float endValue,
      float linearKeyframeProgress, float interpolatedKeyframeProgress, float overallProgress) {
    return offset;
  }
}
