package com.airbnb.lottie.utils;

import android.graphics.Path;
import android.graphics.PointF;
import androidx.annotation.FloatRange;

import com.airbnb.lottie.animation.content.KeyPathElementContent;
import com.airbnb.lottie.model.CubicCurveData;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.content.ShapeData;

import java.util.List;

public class MiscUtils {
  public static PointF addPoints(PointF p1, PointF p2) {
    return new PointF(p1.x + p2.x, p1.y + p2.y);
  }

  public static void getPathFromData(ShapeData shapeData, Path outPath) {
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

  public static float lerp(float a, float b, @FloatRange(from = 0f, to = 1f) float percentage) {
    return a + percentage * (b - a);
  }

  public static double lerp(double a, double b, @FloatRange(from = 0f, to = 1f) double percentage) {
    return a + percentage * (b - a);
  }

  public static int lerp(int a, int b, @FloatRange(from = 0f, to = 1f) float percentage) {
    return (int) (a + percentage * (b - a));
  }

  static int floorMod(float x, float y) {
    return floorMod((int) x, (int) y);
  }

  private static int floorMod(int x, int y) {
    return x - y * floorDiv(x, y);
  }

  private static int floorDiv(int x, int y) {
    int r = x / y;
    boolean sameSign = (x ^ y) >= 0;
    int mod = x % y;
    if (!sameSign && mod != 0) {
      r--;
    }
    return r;
  }

  public static int clamp(int number, int min, int max) {
    return Math.max(min, Math.min(max, number));
  }

  public static float clamp(float number, float min, float max) {
    return Math.max(min, Math.min(max, number));
  }

  public static boolean contains(float number, float rangeMin, float rangeMax) {
    return number >= rangeMin && number <= rangeMax;
  }

  /**
   * Helper method for any {@link KeyPathElementContent} that will check if the content
   * fully matches the keypath then will add itself as the final key, resolve it, and add
   * it to the accumulator list.
   *
   * Any {@link KeyPathElementContent} should call through to this as its implementation of
   * {@link KeyPathElementContent#resolveKeyPath(KeyPath, int, List, KeyPath)}.
   */
  public static void resolveKeyPath(KeyPath keyPath, int depth, List<KeyPath> accumulator,
      KeyPath currentPartialKeyPath, KeyPathElementContent content) {
    if (keyPath.fullyResolvesTo(content.getName(), depth)) {
      currentPartialKeyPath = currentPartialKeyPath.addKey(content.getName());
      accumulator.add(currentPartialKeyPath.resolve(content));
    }
  }
}
