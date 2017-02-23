package com.airbnb.lottie;

class CubicCurveData {
  private final CPointF controlPoint1;
  private final CPointF controlPoint2;
  private final CPointF vertex;

  CubicCurveData() {
    controlPoint1 = new CPointF();
    controlPoint2 = new CPointF();
    vertex = new CPointF();
  }

  CubicCurveData(CPointF controlPoint1, CPointF controlPoint2, CPointF vertex) {
    this.controlPoint1 = controlPoint1;
    this.controlPoint2 = controlPoint2;
    this.vertex = vertex;
  }

  void setControlPoint1(float x, float y) {
    controlPoint1.set(x, y);
  }

  CPointF getControlPoint1() {
    return controlPoint1;
  }

  void setControlPoint2(float x, float y) {
    controlPoint2.set(x, y);
  }

  CPointF getControlPoint2() {
    return controlPoint2;
  }

  void setVertex(float x, float y) {
    vertex.set(x, y);
  }

  CPointF getVertex() {
    return vertex;
  }
}
