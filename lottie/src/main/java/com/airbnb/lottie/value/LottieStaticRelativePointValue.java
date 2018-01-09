package com.airbnb.lottie.value;

import android.graphics.PointF;

/**
 * Static value version of {@link LottiePointRelativeValueCallback}.
 */
public class LottieStaticRelativePointValue extends LottiePointRelativeValueCallback {

  private final PointF offset;

  public LottieStaticRelativePointValue(PointF offset) {
    this.offset = offset;
  }

  @Override
  public PointF getOffset(
      float startFrame, float endFrame,
      PointF startValue, PointF endValue,
      float linearKeyframeProgress, float interpolatedKeyframeProgress,
      float overallProgress) {
    return offset;
  }
}
