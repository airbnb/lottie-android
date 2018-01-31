package com.airbnb.lottie.value;

import android.graphics.PointF;

import com.airbnb.lottie.utils.MiscUtils;

/**
 * {@link LottieValueCallback} that provides a value offset from the original animation
 * rather than an absolute value.
 */
public abstract class LottieRelativePointValueCallback extends LottieValueCallback<PointF> {
  private final PointF point = new PointF();

  @Override
  public final PointF getValue(LottieFrameInfo<PointF> frameInfo) {
    point.set(
        MiscUtils.lerp(
            frameInfo.getStartValue().x,
            frameInfo.getEndValue().x,
            frameInfo.getInterpolatedKeyframeProgress()),
        MiscUtils.lerp(
            frameInfo.getStartValue().y,
            frameInfo.getEndValue().y,
            frameInfo.getInterpolatedKeyframeProgress())
    );

    PointF offset = getOffset(frameInfo);
    point.offset(offset.x, offset.y);
    return point;
  }

  public abstract PointF getOffset(LottieFrameInfo<PointF> frameInfo);
}
