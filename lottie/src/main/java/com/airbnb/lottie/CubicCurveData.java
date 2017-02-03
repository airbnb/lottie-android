package com.airbnb.lottie;

import android.graphics.PointF;

class CubicCurveData {
  private final PointF controlPoint1;
  private final PointF controlPoint2;
  private final PointF vertex;

  CubicCurveData() {
    controlPoint1 = new PointF();
    controlPoint2 = new PointF();
    vertex = new PointF();
  }

  CubicCurveData(PointF controlPoint1, PointF controlPoint2, PointF vertex) {
    this.controlPoint1 = controlPoint1;
    this.controlPoint2 = controlPoint2;
    this.vertex = vertex;
  }

  void setControlPoint1(float x, float y) {
    controlPoint1.set(x, y);
  }

  PointF getControlPoint1() {
    return controlPoint1;
  }

  void setControlPoint2(float x, float y) {
    controlPoint2.set(x, y);
  }

  PointF getControlPoint2() {
    return controlPoint2;
  }

  void setVertex(float x, float y) {
    vertex.set(x, y);
  }

  PointF getVertex() {
    return vertex;
  }
}
