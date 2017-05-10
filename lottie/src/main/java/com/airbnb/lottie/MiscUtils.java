package com.airbnb.lottie;

import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.FloatRange;

class MiscUtils {
  static PointF addPoints(PointF p1, PointF p2) {
    return new PointF(p1.x + p2.x, p1.y + p2.y);
  }

  static void getPathFromData(ShapeData shapeData, Path outPath) {
    outPath.reset();
    PointF initialPoint = shapeData.getInitialPoint();
    outPath.moveTo(initialPoint.x, initialPoint.y);
    PointF currentPoint = new PointF(initialPoint.x, initialPoint.y);
    for (int i = 0; i < shapeData.getCurves().size(); i++) {
      CubicCurveData curveData = shapeData.getCurves().get(i);
      PointF cp1 = curveData.getControlPoint1();
      PointF cp2 = curveData.getControlPoint2();
      PointF vertex = curveData.getVertex();

      if (cp1.equals(currentPoint) && cp2.equals(vertex)) {
        // On some phones like Samsung phones, zero valued control points can cause artifacting.
        // https://github.com/airbnb/lottie-android/issues/275
        //
        // This does its best to add a tiny value to the vertex without affecting the final
        // animation as much as possible.
        // outPath.rMoveTo(0.01f, 0.01f);
        outPath.lineTo(vertex.x, vertex.y);
      } else {
        outPath.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, vertex.x, vertex.y);
      }
      currentPoint.set(vertex.x, vertex.y);
    }
    if (shapeData.isClosed()) {
      outPath.close();
    }
  }

  static float lerp(float a, float b, @FloatRange(from = 0f, to = 1f) float percentage) {
    return a + percentage * (b - a);
  }

  static double lerp(double a, double b, @FloatRange(from = 0f, to = 1f) double percentage) {
    return a + percentage * (b - a);
  }

  static int lerp(int a, int b, @FloatRange(from = 0f, to = 1f) float percentage) {
    return (int) (a + percentage * (b - a));
  }

  static int floorMod(float x, float y) {
    return floorMod((int) x, (int) y);
  }

  /**
   * Copied from Math.floorMod in the Android platform.
   */
  static int floorMod(int x, int y) {
    return x - floorDiv(x, y) * y;
  }

  /**
   * Copied from Math.floorDiv in the Android platform.
   */
  private static int floorDiv(int x, int y) {
    int r = x / y;
    // if the signs are different and modulo not zero, round down
    if ((x ^ y) < 0 && (r * y != x)) {
      r--;
    }
    return r;
  }

  static float clamp(float number, float min, float max) {
    return Math.max(min, Math.min(max, number));
  }
}
