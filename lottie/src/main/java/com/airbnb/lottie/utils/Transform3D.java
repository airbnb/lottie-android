package com.airbnb.lottie.utils;

import android.graphics.Matrix;
import android.graphics.PointF;

/**
 * Utilities for projecting a rotated 3D plane onto Lottie's 2D canvas.
 *
 * <p>This is an orthographic projection. It preserves the coupling between the X, Y, and Z
 * rotations, but it does not provide camera perspective or depth sorting between layers.</p>
 */
public class Transform3D {

  /**
   * Applies a complete transform to {@code outMatrix}.
   *
   * <p>This overload is retained for source compatibility. Rendering code that already owns
   * scratch storage should use the scratch-storage overload of {@code apply3DRotations} to
   * avoid allocations.</p>
   */
  public static void applyTransform(
      Matrix outMatrix,
      PointF anchor,
      PointF position,
      float scaleX,
      float scaleY,
      float rotationX,
      float rotationY,
      float rotationZ,
      float preComputedCosX,
      float preComputedCosY) {
    outMatrix.reset();

    if (position != null && (position.x != 0f || position.y != 0f)) {
      outMatrix.preTranslate(position.x, position.y);
    }

    apply3DRotations(
        outMatrix,
        rotationX,
        rotationY,
        rotationZ,
        preComputedCosX,
        preComputedCosY);

    if (scaleX != 1f || scaleY != 1f) {
      outMatrix.preScale(scaleX, scaleY);
    }

    if (anchor != null && (anchor.x != 0f || anchor.y != 0f)) {
      outMatrix.preTranslate(-anchor.x, -anchor.y);
    }
  }

  /**
   * Applies an orthographic projection of rotations performed in Z, Y, X order.
   *
   * <p>This overload is retained for source compatibility and allocates scratch storage. It
   * should not be used on the rendering hot path.</p>
   */
  public static void apply3DRotations(
      Matrix matrix,
      float rotationX,
      float rotationY,
      float rotationZ,
      float preComputedCosX,
      float preComputedCosY) {
    float radiansX = (float) Math.toRadians(rotationX);
    float radiansY = (float) Math.toRadians(rotationY);
    float radiansZ = (float) Math.toRadians(rotationZ);
    apply3DRotations(
        matrix,
        new Matrix(),
        new float[9],
        (float) Math.sin(radiansX),
        preComputedCosX,
        (float) Math.sin(radiansY),
        preComputedCosY,
        (float) Math.sin(radiansZ),
        (float) Math.cos(radiansZ));
  }

  /**
   * Applies an orthographic projection of rotations performed in Z, Y, X order without
   * allocating objects.
   *
   * @param matrix matrix to which the projected rotation is prepended
   * @param rotationMatrix reusable scratch matrix
   * @param rotationValues reusable array with a length of at least 9
   */
  public static void apply3DRotations(
      Matrix matrix,
      Matrix rotationMatrix,
      float[] rotationValues,
      float sinX,
      float cosX,
      float sinY,
      float cosY,
      float sinZ,
      float cosZ) {
    apply3DRotations(
        matrix,
        rotationMatrix,
        rotationValues,
        sinX,
        cosX,
        sinY,
        cosY,
        sinZ,
        cosZ,
        0f,
        0f);
  }

  /** Applies an orthographic 3D rotation around a 2D pivot without allocating objects. */
  public static void apply3DRotations(
      Matrix matrix,
      Matrix rotationMatrix,
      float[] rotationValues,
      float sinX,
      float cosX,
      float sinY,
      float cosY,
      float sinZ,
      float cosZ,
      float pivotX,
      float pivotY) {
    // A point on the layer starts at z=0. Apply Rz, then Ry, then Rx and discard the
    // resulting z coordinate. Unlike independent cosine scales, these coefficients retain
    // the cross-axis terms when more than one rotation is present.
    float scaleX = cosY * cosZ;
    float skewX = -cosY * sinZ;
    float skewY = cosX * sinZ + sinX * sinY * cosZ;
    float scaleY = cosX * cosZ - sinX * sinY * sinZ;

    rotationValues[Matrix.MSCALE_X] = scaleX;
    rotationValues[Matrix.MSKEW_X] = skewX;
    rotationValues[Matrix.MTRANS_X] = pivotX - scaleX * pivotX - skewX * pivotY;
    rotationValues[Matrix.MSKEW_Y] = skewY;
    rotationValues[Matrix.MSCALE_Y] = scaleY;
    rotationValues[Matrix.MTRANS_Y] = pivotY - skewY * pivotX - scaleY * pivotY;
    rotationValues[Matrix.MPERSP_0] = 0f;
    rotationValues[Matrix.MPERSP_1] = 0f;
    rotationValues[Matrix.MPERSP_2] = 1f;
    rotationMatrix.setValues(rotationValues);
    matrix.preConcat(rotationMatrix);
  }

  /** Returns whether at least one supplied rotation is non-zero. */
  public static boolean has3DRotation(Float rotationX, Float rotationY, Float rotationZ) {
    return (rotationX != null && rotationX != 0f)
        || (rotationY != null && rotationY != 0f)
        || (rotationZ != null && rotationZ != 0f);
  }
}
