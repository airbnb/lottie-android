package com.airbnb.lottie.value;

import android.graphics.PointF;
import androidx.annotation.NonNull;

import com.airbnb.lottie.utils.MiscUtils;

/**
 * {@link LottieValueCallback} that provides a value offset from the original animation
 * rather than an absolute value.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class LottieRelativePointValueCallback extends LottieValueCallback<PointF> {
  private final PointF point = new PointF();

  public LottieRelativePointValueCallback() {
  }

  public LottieRelativePointValueCallback(@NonNull PointF staticValue) {
    super(staticValue);
  }

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

  /**
   * Override this to provide your own offset on every frame.
   */
  public PointF getOffset(LottieFrameInfo<PointF> frameInfo) {
    if (value == null) {
      throw new IllegalArgumentException("You must provide a static value in the constructor " +
          ", call setValue, or override getValue.");
    }
    return value;
  }
}
