package com.airbnb.lottie.value;

import android.graphics.PointF;

import com.airbnb.lottie.utils.MiscUtils;

/**
 * {@link LottieValueCallback} that provides a value offset from the original animation
 * rather than an absolute value.
 */
public abstract class LottieRelativePointValueCallback implements LottieValueCallback<PointF> {
  private final PointF point = new PointF();

  @Override
  public final PointF getValue(
      float sf, float ef, PointF sv, PointF ev, float lkp, float ikp, float p) {
    point.set(MiscUtils.lerp(sv.x, ev.x, ikp), MiscUtils.lerp(sv.y, sv.y, ikp));

    PointF offset = getOffset(sf, ef, sv, ev, lkp, ikp, p);
    point.offset(offset.x, offset.y);
    return point;
  }

  public abstract PointF getOffset(
      float startFrame, float endFrame,
      PointF startValue, PointF endValue,
      float linearKeyframeProgress, float interpolatedKeyframeProgress, float overallProgress);
}
