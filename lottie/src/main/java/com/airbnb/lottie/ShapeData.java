package com.airbnb.lottie;

import android.graphics.PointF;
import android.support.annotation.FloatRange;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ShapeData {
  private final List<CubicCurveData> curves = new ArrayList<>();
  private PointF initialPoint;
  private boolean closed;

  private ShapeData(PointF initialPoint, boolean closed, List<CubicCurveData> curves) {
    this.initialPoint = initialPoint;
    this.closed = closed;
    this.curves.addAll(curves);
  }

  ShapeData() {
  }

  private void setInitialPoint(float x, float y) {
    if (initialPoint == null) {
      initialPoint = new PointF();
    }
    initialPoint.set(x, y);
  }

  PointF getInitialPoint() {
    return initialPoint;
  }

  boolean isClosed() {
    return closed;
  }

  List<CubicCurveData> getCurves() {
    return curves;
  }

  void interpolateBetween(ShapeData shapeData1, ShapeData shapeData2,
      @FloatRange(from = 0f, to = 1f) float percentage) {
    if (initialPoint == null) {
      initialPoint = new PointF();
    }
    closed = shapeData1.isClosed() || shapeData2.isClosed();

    if (!curves.isEmpty() && curves.size() != shapeData1.getCurves().size()
        && curves.size() != shapeData2.getCurves().size()) {
      throw new IllegalStateException("Curves must have the same number of control points. This: "
          + getCurves().size()
          + "\tShape 1: " + shapeData1.getCurves().size() + "\tShape 2: "
          + shapeData2.getCurves().size());
    } else if (curves.isEmpty()) {
      for (int i = shapeData1.getCurves().size() - 1; i >= 0; i--) {
        curves.add(new CubicCurveData());
      }
    }

    PointF initialPoint1 = shapeData1.getInitialPoint();
    PointF initialPoint2 = shapeData2.getInitialPoint();

    setInitialPoint(MiscUtils.lerp(initialPoint1.x, initialPoint2.x, percentage),
        MiscUtils.lerp(initialPoint1.y, initialPoint2.y, percentage));

    for (int i = curves.size() - 1; i >= 0; i--) {
      CubicCurveData curve1 = shapeData1.getCurves().get(i);
      CubicCurveData curve2 = shapeData2.getCurves().get(i);

      PointF cp11 = curve1.getControlPoint1();
      PointF cp21 = curve1.getControlPoint2();
      PointF vertex1 = curve1.getVertex();

      PointF cp12 = curve2.getControlPoint1();
      PointF cp22 = curve2.getControlPoint2();
      PointF vertex2 = curve2.getVertex();

      curves.get(i).setControlPoint1(
          MiscUtils.lerp(cp11.x, cp12.x, percentage), MiscUtils.lerp(cp11.y, cp12.y,
              percentage));
      curves.get(i).setControlPoint2(
          MiscUtils.lerp(cp21.x, cp22.x, percentage), MiscUtils.lerp(cp21.y, cp22.y,
              percentage));
      curves.get(i).setVertex(
          MiscUtils.lerp(vertex1.x, vertex2.x, percentage), MiscUtils.lerp(vertex1.y, vertex2.y,
              percentage));
    }
  }

  @Override public String toString() {
    return "ShapeData{" + "numCurves=" + curves.size() +
        "closed=" + closed +
        '}';
  }

  static class Factory implements AnimatableValue.Factory<ShapeData> {
    static final ShapeData.Factory INSTANCE = new Factory();

    private Factory() {
    }

    @Override public ShapeData valueFromObject(Object object, float scale) {
      JSONObject pointsData = null;
      if (object instanceof JSONArray) {
        Object firstObject = ((JSONArray) object).opt(0);
        if (firstObject instanceof JSONObject && ((JSONObject) firstObject).has("v")) {
          pointsData = (JSONObject) firstObject;
        }
      } else if (object instanceof JSONObject && ((JSONObject) object).has("v")) {
        pointsData = (JSONObject) object;
      }

      if (pointsData == null) {
        return null;
      }

      JSONArray pointsArray = pointsData.optJSONArray("v");
      JSONArray inTangents = pointsData.optJSONArray("i");
      JSONArray outTangents = pointsData.optJSONArray("o");
      boolean closed = pointsData.optBoolean("c", false);

      if (pointsArray == null || inTangents == null || outTangents == null ||
          pointsArray.length() != inTangents.length() ||
          pointsArray.length() != outTangents.length()) {
        throw new IllegalStateException(
            "Unable to process points array or tangents. " + pointsData);
      } else if (pointsArray.length() == 0) {
        return new ShapeData(new PointF(), false, Collections.<CubicCurveData>emptyList());
      }

      int length = pointsArray.length();
      PointF vertex = vertexAtIndex(0, pointsArray);
      vertex.x *= scale;
      vertex.y *= scale;
      PointF initialPoint = vertex;
      List<CubicCurveData> curves = new ArrayList<>(length);

      for (int i = 1; i < length; i++) {
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

        curves.add(new CubicCurveData(shapeCp1, shapeCp2, vertex));
      }

      if (closed) {
        vertex = vertexAtIndex(0, pointsArray);
        PointF previousVertex = vertexAtIndex(length - 1, pointsArray);
        PointF cp1 = vertexAtIndex(length - 1, outTangents);
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

        curves.add(new CubicCurveData(shapeCp1, shapeCp2, vertex));
      }
      return new ShapeData(initialPoint, closed, curves);
    }

    private static PointF vertexAtIndex(int idx, JSONArray points) {
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
  }
}
