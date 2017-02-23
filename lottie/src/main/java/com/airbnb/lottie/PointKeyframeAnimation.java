package com.airbnb.lottie;

import java.util.List;

class PointKeyframeAnimation extends KeyframeAnimation<CPointF> {
  private final CPointF point = new CPointF();

  PointKeyframeAnimation(List<Keyframe<CPointF>> keyframes) {
    super(keyframes);
  }

  @Override public CPointF getValue(Keyframe<CPointF> keyframe, float keyframeProgress) {
    CPointF startPoint = keyframe.startValue;
    CPointF endPoint = keyframe.endValue;

    point.set(startPoint.x() + keyframeProgress * (endPoint.x() - startPoint.x()),
        startPoint.y() + keyframeProgress * (endPoint.y() - startPoint.y()));
    return point;
  }
}
