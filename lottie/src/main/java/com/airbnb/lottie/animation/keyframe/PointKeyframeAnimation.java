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
    if (keyframe.startValue == null || keyframe.endValue == null) {
      throw new IllegalStateException("Missing values for keyframe.");
    }

    PointF startPoint = keyframe.startValue;
    PointF endPoint = keyframe.endValue;

    if (valueCallback != null) {
      //noinspection ConstantConditions
      return valueCallback.getValueInternal(keyframe.startFrame, keyframe.endFrame, startPoint,
          endPoint, keyframeProgress, getLinearCurrentKeyframeProgress(), getProgress());
    }

    point.set(startPoint.x + keyframeProgress * (endPoint.x - startPoint.x),
        startPoint.y + keyframeProgress * (endPoint.y - startPoint.y));
    return point;
  }
}
