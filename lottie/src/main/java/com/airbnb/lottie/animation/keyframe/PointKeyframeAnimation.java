package com.airbnb.lottie.animation.keyframe;

import android.graphics.PointF;

import com.airbnb.lottie.value.Keyframe;

import java.util.List;

public class PointKeyframeAnimation extends KeyframeAnimation<PointF> {
  private final PointF point = new PointF();

  public PointKeyframeAnimation(List<Keyframe<PointF>> keyframes) {
    super(keyframes);
  }

  @Override public PointF getValue(Keyframe<PointF> keyframe, float keyframeProgress) {
    return getValue(keyframe, keyframeProgress, keyframeProgress, keyframeProgress);
  }

  @Override protected PointF getValue(Keyframe<PointF> keyframe, float linearKeyframeProgress, float xKeyframeProgress, float yKeyframeProgress) {
    if (keyframe.startValue == null || keyframe.endValue == null) {
      throw new IllegalStateException("Missing values for keyframe.");
    }

    PointF startPoint = keyframe.startValue;
    PointF endPoint = keyframe.endValue;

    if (valueCallback != null) {
      //noinspection ConstantConditions
      PointF value = valueCallback.getValueInternal(keyframe.startFrame, keyframe.endFrame, startPoint,
          endPoint, linearKeyframeProgress, getLinearCurrentKeyframeProgress(), getProgress());
      if (value != null) {
        return value;
      }
    }

    point.set(startPoint.x + xKeyframeProgress * (endPoint.x - startPoint.x),
        startPoint.y + yKeyframeProgress * (endPoint.y - startPoint.y));
    return point;
  }
}
