package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.animation.Point3F;
import com.airbnb.lottie.value.Keyframe;

import java.util.List;

public class Point3KeyframeAnimation extends KeyframeAnimation<Point3F> {
  private final Point3F point = new Point3F();

  public Point3KeyframeAnimation(List<Keyframe<Point3F>> keyframes) {
    super(keyframes);
  }

  @Override public Point3F getValue(Keyframe<Point3F> keyframe, float keyframeProgress) {
    return getValue(keyframe, keyframeProgress, keyframeProgress, keyframeProgress);
  }

  /**
   * TODO: split keyframes doesn't really make sense with Point3F animations.
   * Lottie currently only supports split path with separate X and Y animations
   * but Point3F has a Z dimension.
   * This may not matter because Point3F is only used for camera layers.
   */
  @Override protected Point3F getValue(Keyframe<Point3F> keyframe, float linearKeyframeProgress,
      float xKeyframeProgress, float yKeyframeProgress) {
    if (keyframe.startValue == null || keyframe.endValue == null) {
      throw new IllegalStateException("Missing values for keyframe.");
    }

    Point3F startPoint = keyframe.startValue;
    Point3F endPoint = keyframe.endValue;

    if (valueCallback != null) {
      //noinspection ConstantConditions
      Point3F value = valueCallback.getValueInternal(keyframe.startFrame, keyframe.endFrame, startPoint,
          endPoint, linearKeyframeProgress, getLinearCurrentKeyframeProgress(), getProgress());
      if (value != null) {
        return value;
      }
    }

    point.set(startPoint.x + xKeyframeProgress * (endPoint.x - startPoint.x),
        startPoint.y + yKeyframeProgress * (endPoint.y - startPoint.y),
        startPoint.z + yKeyframeProgress * (endPoint.z - startPoint.z));
    return point;
  }
}