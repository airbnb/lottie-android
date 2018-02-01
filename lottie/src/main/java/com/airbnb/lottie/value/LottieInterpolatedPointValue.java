package com.airbnb.lottie.value;

import android.graphics.PointF;
import android.view.animation.Interpolator;

import com.airbnb.lottie.utils.MiscUtils;

@SuppressWarnings("unused")
public class LottieInterpolatedPointValue extends LottieInterpolatedValue<PointF> {
  private final PointF point = new PointF();

  public LottieInterpolatedPointValue(PointF startValue, PointF endValue) {
    super(startValue, endValue);
  }

  public LottieInterpolatedPointValue(PointF startValue, PointF endValue, Interpolator interpolator) {
    super(startValue, endValue, interpolator);
  }

  @Override PointF interpolateValue(PointF startValue, PointF endValue, float progress) {
    point.set(
        MiscUtils.lerp(startValue.x, endValue.x, progress),
        MiscUtils.lerp(startValue.y, endValue.y, progress)
    );
    return point;
  }
}
