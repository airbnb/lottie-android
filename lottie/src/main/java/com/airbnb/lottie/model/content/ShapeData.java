package com.airbnb.lottie.model.content;

import android.graphics.PointF;
import androidx.annotation.FloatRange;

import com.airbnb.lottie.L;
import com.airbnb.lottie.model.CubicCurveData;
import com.airbnb.lottie.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

public class ShapeData {
  private final List<CubicCurveData> curves = new ArrayList<>();
  private PointF initialPoint;
  private boolean closed;

  public ShapeData(PointF initialPoint, boolean closed, List<CubicCurveData> curves) {
    this.initialPoint = initialPoint;
    this.closed = closed;
    this.curves.addAll(curves);
  }

  public ShapeData() {
  }

  private void setInitialPoint(float x, float y) {
    if (initialPoint == null) {
      initialPoint = new PointF();
    }
    initialPoint.set(x, y);
  }

  public PointF getInitialPoint() {
    return initialPoint;
  }

  public boolean isClosed() {
    return closed;
  }

  public List<CubicCurveData> getCurves() {
    return curves;
  }

  public void interpolateBetween(ShapeData shapeData1, ShapeData shapeData2,
      @FloatRange(from = 0f, to = 1f) float percentage) {
    if (initialPoint == null) {
      initialPoint = new PointF();
    }
    closed = shapeData1.isClosed() || shapeData2.isClosed();


    if (shapeData1.getCurves().size() != shapeData2.getCurves().size()) {
      L.warn("Curves must have the same number of control points. Shape 1: " +
          shapeData1.getCurves().size() + "\tShape 2: " + shapeData2.getCurves().size());
    }
    
    if (curves.isEmpty()) {
      int points = Math.min(shapeData1.getCurves().size(), shapeData2.getCurves().size());
      for (int i = 0; i < points; i++) {
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
}
