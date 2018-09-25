package com.airbnb.lottie.model;

import android.graphics.PointF;
import androidx.annotation.RestrictTo;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

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

  public PointF getVertex() {
    return vertex;
  }
}
