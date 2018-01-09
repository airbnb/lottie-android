package com.airbnb.lottie.value;

/**
 * Static value version of {@link LottieIntegerRelativeValueCallback}.
 */
public class LottieStaticRelativeIntegerValue extends LottieIntegerRelativeValueCallback {

  private final Integer offset;

  public LottieStaticRelativeIntegerValue(Integer offset) {
    this.offset = offset;
  }


  @Override
  public Integer getOffset(
      float startFrame, float endFrame,
      Integer startValue, Integer endValue,
      float linearKeyframeProgress, float interpolatedKeyframeProgress,
      float overallProgress) {
    return offset;
  }
}
