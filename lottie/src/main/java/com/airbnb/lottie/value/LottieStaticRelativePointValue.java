package com.airbnb.lottie.value;

import android.graphics.PointF;

/**
 * Static value version of {@link LottieRelativePointValueCallback}.
 */
public class LottieStaticRelativePointValue extends LottieRelativePointValueCallback {

  private final PointF offset;

  public LottieStaticRelativePointValue(PointF offset) {
    this.offset = offset;
  }

  @Override
  public PointF getOffset(LottieFrameInfo<PointF> frameInfo) {
    return offset;
  }
}
