package com.airbnb.lottie.value;

import android.graphics.PointF;

public class LottieStaticPositionOffsetValueCallback extends LottiePositionOffsetValueCallback {

  private final PointF offset;

  public LottieStaticPositionOffsetValueCallback(PointF offset) {
    this.offset = offset;
  }

  @Override
  public PointF getOffset(float startFrame, float endFrame, PointF startValue, PointF endValue,
      float linearKeyframeProgress, float interpolatedKeyframeProgress, float overallProgress) {
    return offset;
  }
}
