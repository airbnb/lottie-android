package com.airbnb.lottie;

import android.graphics.Path;
import android.graphics.PointF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class AnimatableShapeValue extends BaseAnimatableValue<ShapeData, Path> {
  private final Path convertTypePath = new Path();

  AnimatableShapeValue(JSONObject json, LottieComposition composition)
      throws JSONException {
    super(json, composition, true);
  }

  @Override public ShapeData valueFromObject(Object object, float scale) throws JSONException {
    JSONObject pointsData = null;
    if (object instanceof JSONArray) {
      try {
        Object firstObject = ((JSONArray) object).get(0);
        if (firstObject instanceof JSONObject && ((JSONObject) firstObject).has("v")) {
          pointsData = (JSONObject) firstObject;
        }
      } catch (JSONException e) {
        throw new IllegalStateException("Unable to get shape. " + object);
      }
    } else if (object instanceof JSONObject && ((JSONObject) object).has("v")) {
      pointsData = (JSONObject) object;
    }

    if (pointsData == null) {
      return null;
    }

    JSONArray pointsArray = null;
    JSONArray inTangents = null;
    JSONArray outTangents = null;
    boolean closed = false;
    if (pointsData.has("v")) {
      pointsArray = pointsData.getJSONArray("v");
    }
    if (pointsData.has("i")) {
      inTangents = pointsData.getJSONArray("i");
    }
    if (pointsData.has("o")) {
      outTangents = pointsData.getJSONArray("o");
    }

    if (pointsData.has("c")) {
      // Bodymovin < 4.4 uses "closed" one level up in the json so it is passed in to the
      // constructor.
      // Bodymovin 4.4+ has closed here.
      closed = pointsData.getBoolean("c");
    }

    if (pointsArray == null || inTangents == null || outTangents == null ||
        pointsArray.length() != inTangents.length() ||
        pointsArray.length() != outTangents.length()) {
      throw new IllegalStateException("Unable to process points array or tangents. " + pointsData);
    }

    ShapeData shape = new ShapeData();

    PointF vertex = vertexAtIndex(0, pointsArray);
    vertex.x *= scale;
    vertex.y *= scale;
    shape.setInitialPoint(vertex);

    for (int i = 1; i < pointsArray.length(); i++) {
      vertex = vertexAtIndex(i, pointsArray);
      PointF previousVertex = vertexAtIndex(i - 1, pointsArray);
      PointF cp1 = vertexAtIndex(i - 1, outTangents);
      PointF cp2 = vertexAtIndex(i, inTangents);

      PointF shapeCp1 = MiscUtils.addPoints(previousVertex, cp1);
      PointF shapeCp2 = MiscUtils.addPoints(vertex, cp2);

      shapeCp1.x *= scale;
      shapeCp1.y *= scale;
      shapeCp2.x *= scale;
      shapeCp2.y *= scale;
      vertex.x *= scale;
      vertex.y *= scale;

      shape.addCurve(new CubicCurveData(shapeCp1, shapeCp2, vertex));
    }

    if (closed) {
      vertex = vertexAtIndex(0, pointsArray);
      PointF previousVertex = vertexAtIndex(pointsArray.length() - 1, pointsArray);
      PointF cp1 = vertexAtIndex(pointsArray.length() - 1, outTangents);
      PointF cp2 = vertexAtIndex(0, inTangents);

      PointF shapeCp1 = MiscUtils.addPoints(previousVertex, cp1);
      PointF shapeCp2 = MiscUtils.addPoints(vertex, cp2);

      if (scale != 1f) {
        shapeCp1.x *= scale;
        shapeCp1.y *= scale;
        shapeCp2.x *= scale;
        shapeCp2.y *= scale;
        vertex.x *= scale;
        vertex.y *= scale;
      }

      shape.addCurve(new CubicCurveData(shapeCp1, shapeCp2, vertex));
      shape.setClosed(true);
    }
    return shape;

  }

  private PointF vertexAtIndex(int idx, JSONArray points) {
    if (idx >= points.length()) {
      throw new IllegalArgumentException(
          "Invalid index " + idx + ". There are only " + points.length() + " points.");
    }

    try {
      JSONArray pointArray = points.getJSONArray(idx);
      Object x = pointArray.get(0);
      Object y = pointArray.get(1);
      return new PointF(
          x instanceof Double ? new Float((Double) x) : (int) x,
          y instanceof Double ? new Float((Double) y) : (int) y);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to get point.", e);
    }
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
