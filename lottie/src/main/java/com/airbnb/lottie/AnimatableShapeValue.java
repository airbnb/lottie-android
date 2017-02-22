package com.airbnb.lottie;

import android.graphics.Path;
import android.graphics.PointF;

import org.json.JSONArray;
import org.json.JSONObject;

class AnimatableShapeValue extends BaseAnimatableValue<ShapeData, Path> {
  private final Path convertTypePath = new Path();

  AnimatableShapeValue(JSONObject json, LottieComposition composition) {
    super(json, composition, true);
  }

  @Override public ShapeData valueFromObject(Object object, float scale) {
    return ShapeData.Factory.newInstance(object, scale, this);
  }

  PointF vertexAtIndex(int idx, JSONArray points) {
    if (idx >= points.length()) {
      throw new IllegalArgumentException(
          "Invalid index " + idx + ". There are only " + points.length() + " points.");
    }

    JSONArray pointArray = points.optJSONArray(idx);
    Object x = pointArray.opt(0);
    Object y = pointArray.opt(1);
    return new PointF(
        x instanceof Double ? new Float((Double) x) : (int) x,
        y instanceof Double ? new Float((Double) y) : (int) y);
  }

  @Override public BaseKeyframeAnimation<?, Path> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(convertType(initialValue));
    }

    return new ShapeKeyframeAnimation(keyframes);
  }

  @Override Path convertType(ShapeData shapeData) {
    convertTypePath.reset();
    MiscUtils.getPathFromData(shapeData, convertTypePath);
    return convertTypePath;
  }
}
