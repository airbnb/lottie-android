package com.airbnb.lottie.utils;

import android.graphics.Matrix;
import android.graphics.PointF;

/**
 * 3D transformation utility class that mimics iOS CATransform3D implementation
 * Provides more accurate 3D rotation calculations, especially for Z-axis rotation
 */
public class Transform3D {

    /**
     * Applies 3D transformation to the given matrix following Lottie Android's transformation order
     * This method reuses the provided matrix to avoid object allocation
     *
     * @param outMatrix Output matrix to receive the transformation
     * @param tempMatrix Temporary matrix for intermediate calculations (will be modified)
     * @param anchor Anchor point
     * @param position Position
     * @param scaleX X-axis scale
     * @param scaleY Y-axis scale
     * @param rotationX X-axis rotation (degrees)
     * @param rotationY Y-axis rotation (degrees)
     * @param rotationZ Z-axis rotation (degrees)
     */
    public static void applyTransform(
            Matrix outMatrix,
            Matrix tempMatrix,
            PointF anchor,
            PointF position,
            float scaleX,
            float scaleY,
            float rotationX,
            float rotationY,
            float rotationZ) {

        outMatrix.reset();

        // Follow original Lottie Android order: position → rotation → scale → anchor

        // 1. Apply position transformation
        if (position != null && (position.x != 0 || position.y != 0)) {
            outMatrix.preTranslate(position.x, position.y);
        }

        // 2. Apply 3D rotation (rotate directly without anchor, as anchor is handled at the end)
        if (rotationZ != 0) {
          outMatrix.preRotate(rotationZ);
        }
        if (rotationY != 0) {
          applyYRotation(outMatrix, tempMatrix, rotationY);
        }
        if (rotationX != 0) {
          applyXRotation(outMatrix, tempMatrix, rotationX);
        }
        
        // 3. Apply scale (Note: Lottie's scale doesn't need to be divided by 100, use ScaleXY values directly)
        if (scaleX != 1.0f || scaleY != 1.0f) {
            outMatrix.preScale(scaleX, scaleY);
        }

        // 4. Finally translate to negative anchor position (consistent with original code)
        if (anchor != null && (anchor.x != 0 || anchor.y != 0)) {
            outMatrix.preTranslate(-anchor.x, -anchor.y);
        }
    }
    
    
    /**
     * Apply X-axis rotation
     * On a 2D plane, X-axis rotation primarily affects Y-direction scaling
     *
     * @param matrix Input/output matrix to be modified
     * @param tempMatrix Temporary matrix for intermediate calculations
     * @param degrees Rotation angle in degrees
     */
    private static void applyXRotation(Matrix matrix, Matrix tempMatrix, float degrees) {
        // X-axis rotation is primarily represented as Y-direction perspective scaling in 2D projection
        float radians = (float) Math.toRadians(degrees);
        float cosX = (float) Math.cos(radians);

        // Use tempMatrix to store the current state
        tempMatrix.set(matrix);

        // Apply Y-direction scale transformation (projection of X-axis rotation)
        tempMatrix.preScale(1f, cosX);

        // Copy result back to original matrix
        matrix.set(tempMatrix);
    }

    /**
     * Apply Y-axis rotation
     * On a 2D plane, Y-axis rotation primarily affects X-direction scaling
     *
     * @param matrix Input/output matrix to be modified
     * @param tempMatrix Temporary matrix for intermediate calculations
     * @param degrees Rotation angle in degrees
     */
    private static void applyYRotation(Matrix matrix, Matrix tempMatrix, float degrees) {
        // Y-axis rotation is primarily represented as X-direction perspective scaling in 2D projection
        float radians = (float) Math.toRadians(degrees);
        float cosY = (float) Math.cos(radians);

        // Use tempMatrix to store the current state
        tempMatrix.set(matrix);

        // Apply X-direction scale transformation (projection of Y-axis rotation)
        tempMatrix.preScale(cosY, 1f);

        // Copy result back to original matrix
        matrix.set(tempMatrix);
    }
    
    /**
     * Check if there is 3D transformation
     */
    public static boolean has3DRotation(Float rotationX, Float rotationY, Float rotationZ) {
        return (rotationX != null && rotationX != 0) ||
               (rotationY != null && rotationY != 0) ||
               (rotationZ != null && rotationZ != 0);
    }
}
