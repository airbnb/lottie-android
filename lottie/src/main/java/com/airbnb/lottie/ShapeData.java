package com.airbnb.lottie;

import android.graphics.PointF;
import android.support.annotation.FloatRange;

import java.util.ArrayList;
import java.util.List;

class ShapeData {
  private final List<CubicCurveData> curves = new ArrayList<>();
  private PointF initialPoint;
  private boolean closed;

  void setInitialPoint(PointF initialPoint) {
    this.initialPoint = initialPoint;
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

  void addCurve(CubicCurveData curve) {
    curves.add(curve);
  }

  boolean isClosed() {
    return closed;
  }

  void setClosed(boolean closed) {
    this.closed = closed;
  }

  List<CubicCurveData> getCurves() {
    return curves;
  }

  void interpolateBetween(ShapeData shapeData1, ShapeData shapeData2,
      @FloatRange(from = 0f, to = 1f) float percentage) {
    if (initialPoint == null) {
      initialPoint = new PointF();
    }
    setClosed(shapeData1.isClosed() || shapeData2.isClosed());

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

    setInitialPoint(MiscUtils.lerp(initialPoint1.x, initialPoint2.x, percentage), MiscUtils.lerp(initialPoint1.y,
        initialPoint2.y, percentage));

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
}
