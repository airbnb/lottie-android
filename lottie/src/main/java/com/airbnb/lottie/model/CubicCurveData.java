package com.airbnb.lottie.model;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

import android.graphics.PointF;

import androidx.annotation.RestrictTo;

/**
 * One cubic path operation. CubicCurveData is structured such that it is easy to iterate through
 * it and build a path. However, it is modeled differently than most path operations.
 *
 * CubicCurveData
 * |                     - vertex
 * |                   /
 * |    cp1          cp2
 * |   /
 * |  |
 * | /
 * --------------------------
 *
 * When incrementally building a path, it will already have a "current point" so that is
 * not captured in this data structure.
 * The control points here represent {@link android.graphics.Path#cubicTo(float, float, float, float, float, float)}.
 *
 * Most path operations are centered around a vertex and its in control point and out control point like this:
 * |           outCp
 * |          /
 * |         |
 * |         v
 * |        /
 * |      inCp
 * --------------------------
 */
@RestrictTo(LIBRARY)
public class CubicCurveData {
  private final PointF controlPoint1;
  private final PointF controlPoint2;
  private final PointF vertex;

  public CubicCurveData() {
    controlPoint1 = new PointF();
    controlPoint2 = new PointF();
    vertex = new PointF();
  }

  public CubicCurveData(PointF controlPoint1, PointF controlPoint2, PointF vertex) {
    this.controlPoint1 = controlPoint1;
    this.controlPoint2 = controlPoint2;
    this.vertex = vertex;
  }

  public void setControlPoint1(float x, float y) {
    controlPoint1.set(x, y);
  }

  public PointF getControlPoint1() {
    return controlPoint1;
  }

  public void setControlPoint2(float x, float y) {
    controlPoint2.set(x, y);
  }

  public PointF getControlPoint2() {
    return controlPoint2;
  }

  public void setVertex(float x, float y) {
    vertex.set(x, y);
  }

  public void setFrom(CubicCurveData curveData) {
    setVertex(curveData.vertex.x, curveData.vertex.y);
    setControlPoint1(curveData.controlPoint1.x, curveData.controlPoint1.y);
    setControlPoint2(curveData.controlPoint2.x, curveData.controlPoint2.y);
  }

  public PointF getVertex() {
    return vertex;
  }
}
