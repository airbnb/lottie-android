package com.airbnb.lottie;

import android.graphics.Path;
import android.graphics.PathMeasure;

import java.util.List;

class PathKeyframeAnimation extends KeyframeAnimation<CPointF> {
  private final CPointF point = new CPointF();
  private final float[] pos = new float[2];
  private IPathKeyframe pathMeasureKeyframe;
  private PathMeasure pathMeasure;

  PathKeyframeAnimation(List<? extends Keyframe<CPointF>> keyframes) {
    super(keyframes);
  }

  @Override public CPointF getValue(Keyframe<CPointF> keyframe, float keyframeProgress) {
    IPathKeyframe pathKeyframe = (PathKeyframe) keyframe;
    Path path = pathKeyframe.getPath();
    if (path == null) {
      return keyframe.startValue;
    }

    if (pathMeasureKeyframe != pathKeyframe) {
      pathMeasure = new PathMeasure(path, false);
      pathMeasureKeyframe = pathKeyframe;
    }

    pathMeasure.getPosTan(keyframeProgress * pathMeasure.getLength(), pos, null);
    point.set(pos[0], pos[1]);
    return point;
  }
}
