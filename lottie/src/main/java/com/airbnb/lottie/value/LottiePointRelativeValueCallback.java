package com.airbnb.lottie.value;

import android.graphics.PointF;

/**
 * {@link LottieValueCallback} that provides a value offset from the original animation
 * rather than an absolute value.
 */
public abstract class LottiePointRelativeValueCallback implements LottieValueCallback<PointF> {
  private final PointF point = new PointF();

  @Override
  public final PointF getValue(
      float startFrame, float endFrame,
      PointF startValue, PointF endValue,
      float linearKeyframeProgress, float interpolatedKeyframeProgress, float overallProgress) {
    PointF offset = getOffset(startFrame, endFrame, startValue, endValue,
        linearKeyframeProgress, interpolatedKeyframeProgress, overallProgress);
    point.set(startValue.x + interpolatedKeyframeProgress * (endValue.x - startValue.x),
        startValue.y + interpolatedKeyframeProgress * (endValue.y - startValue.y));
    point.offset(offset.x, offset.y);
    return point;
  }

  public abstract PointF getOffset(
      float startFrame, float endFrame,
      PointF startValue, PointF endValue,
      float linearKeyframeProgress, float interpolatedKeyframeProgress, float overallProgress);
}
