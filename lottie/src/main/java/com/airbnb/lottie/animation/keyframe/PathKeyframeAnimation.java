package com.airbnb.lottie.animation.keyframe;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;

import com.airbnb.lottie.value.Keyframe;

import java.util.List;

public class PathKeyframeAnimation extends KeyframeAnimation<PointF> {
  private final PointF point = new PointF();
  private final float[] pos = new float[2];
  private final float[] tangent = new float[2];
  private final PathMeasure pathMeasure = new PathMeasure();
  private PathKeyframe pathMeasureKeyframe;

  public PathKeyframeAnimation(List<? extends Keyframe<PointF>> keyframes) {
    super(keyframes);
  }

  @Override public PointF getValue(Keyframe<PointF> keyframe, float keyframeProgress) {
    PathKeyframe pathKeyframe = (PathKeyframe) keyframe;
    Path path = pathKeyframe.getPath();
    if (path == null) {
      return keyframe.startValue;
    }

    if (valueCallback != null) {
      PointF value = valueCallback.getValueInternal(pathKeyframe.startFrame, pathKeyframe.endFrame,
          pathKeyframe.startValue, pathKeyframe.endValue, getLinearCurrentKeyframeProgress(),
          keyframeProgress, getProgress());
      if (value != null) {
        return value;
      }
    }

    if (pathMeasureKeyframe != pathKeyframe) {
      pathMeasure.setPath(path, false);
      pathMeasureKeyframe = pathKeyframe;
    }

    // allow bounce easings to calculate positions outside the path
    // by using the tangent at the extremities

    float length = pathMeasure.getLength();

    if (keyframeProgress < 0) {
      pathMeasure.getPosTan(0, pos, tangent);
      float tangentAmount = keyframeProgress * length;
      point.set(pos[0] + tangent[0] * tangentAmount, pos[1] + tangent[1] * tangentAmount);
    } else if (keyframeProgress > 1) {
      pathMeasure.getPosTan(length, pos, tangent);
      float tangentAmount = (keyframeProgress - 1) * length;
      point.set(pos[0] + tangent[0] * tangentAmount, pos[1] + tangent[1] * tangentAmount);
    } else {
      pathMeasure.getPosTan(keyframeProgress * length, pos, null);
      point.set(pos[0], pos[1]);
    }
    return point;
  }
}
