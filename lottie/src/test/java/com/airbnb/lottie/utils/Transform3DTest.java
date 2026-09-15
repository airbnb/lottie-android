package com.airbnb.lottie.utils;

import android.graphics.Matrix;

import com.airbnb.lottie.BaseTest;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class Transform3DTest extends BaseTest {

  private static final float EPSILON = 0.0001f;

  @Test
  public void combinedXYRotationRetainsCrossAxisContribution() {
    Matrix matrix = new Matrix();
    rotate(matrix, 45f, 45f, 0f);
    float[] points = {1f, 0f, 0f, 1f};
    matrix.mapPoints(points);
    float diagonal = (float) Math.sqrt(0.5);
    assertArrayEquals(new float[]{diagonal, 0.5f, 0f, diagonal}, points, EPSILON);
  }

  @Test
  public void projectionMatchesSequentialRotationsInThreeDimensions() {
    float[][] angles = {{0, 0, 0}, {60, 0, 0}, {0, -60, 0}, {0, 0, 90}, {35, -50, 70}, {-90, 45, 180}};
    for (float[] angle : angles) {
      Matrix matrix = new Matrix();
      rotate(matrix, angle[0], angle[1], angle[2]);
      float[] points = {3f, -7f, -2f, 5f};
      float[] expected = points.clone();
      for (int i = 0; i < expected.length; i += 2) {
        // Rotate the actual 3D vector one axis at a time; only project at the end.
        double x = expected[i];
        double y = expected[i + 1];
        double rz = Math.toRadians(angle[2]);
        double rotatedX = x * Math.cos(rz) - y * Math.sin(rz);
        y = x * Math.sin(rz) + y * Math.cos(rz);
        double ry = Math.toRadians(angle[1]);
        x = rotatedX * Math.cos(ry);
        double z = -rotatedX * Math.sin(ry);
        double rx = Math.toRadians(angle[0]);
        y = y * Math.cos(rx) - z * Math.sin(rx);
        expected[i] = (float) x;
        expected[i + 1] = (float) y;
      }
      matrix.mapPoints(points);
      assertArrayEquals(expected, points, EPSILON);
    }
  }

  private static void rotate(Matrix matrix, float x, float y, float z) {
    Transform3D.apply3DRotations(matrix, x, y, z, (float) Math.cos(Math.toRadians(x)), (float) Math.cos(Math.toRadians(y)));
  }
}
