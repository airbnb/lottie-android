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
     * @param anchor Anchor point
     * @param position Position
     * @param scaleX X-axis scale
     * @param scaleY Y-axis scale
     * @param rotationX X-axis rotation (degrees)
     * @param rotationY Y-axis rotation (degrees)
     * @param rotationZ Z-axis rotation (degrees)
     * @param preComputedCosX Pre-computed cos(rotationX) to avoid redundant calculation
     * @param preComputedCosY Pre-computed cos(rotationY) to avoid redundant calculation
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
          applyYRotation(outMatrix, preComputedCosY);
        }
        if (rotationX != 0) {
          applyXRotation(outMatrix, preComputedCosX);
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
     * Apply 3D rotations (X, Y, Z) to the matrix
     * This method can be used independently for repeater or other scenarios
     *
     * @param matrix Output matrix to receive the rotation transformation
     * @param rotationX X-axis rotation in degrees
     * @param rotationY Y-axis rotation in degrees
     * @param rotationZ Z-axis rotation in degrees
     * @param preComputedCosX Pre-computed cos(rotationX) to avoid redundant calculation
     * @param preComputedCosY Pre-computed cos(rotationY) to avoid redundant calculation
     */
    public static void apply3DRotations(
            Matrix matrix,
            float rotationX,
            float rotationY,
            float rotationZ,
            float preComputedCosX,
            float preComputedCosY) {

        // Apply rotations in order: Z -> Y -> X
        if (rotationZ != 0) {
            matrix.preRotate(rotationZ);
        }
        if (rotationY != 0) {
            applyYRotation(matrix, preComputedCosY);
        }
        if (rotationX != 0) {
            applyXRotation(matrix, preComputedCosX);
        }
    }

    /**
     * Apply X-axis rotation using pre-computed cosine value
     * On a 2D plane, X-axis rotation primarily affects Y-direction scaling
     * Optimized version that directly modifies matrix values to avoid allocation
     *
     * @param matrix Input/output matrix to be modified
     * @param cosX Pre-computed cos(rotationX) value
     */
    private static void applyXRotation(Matrix matrix, float cosX) {
        // X-axis rotation is primarily represented as Y-direction perspective scaling in 2D projection
        // Directly scale Y-direction without matrix copy
        matrix.preScale(1f, cosX);
    }

    /**
     * Apply Y-axis rotation using pre-computed cosine value
     * On a 2D plane, Y-axis rotation primarily affects X-direction scaling
     * Optimized version that directly modifies matrix values to avoid allocation
     *
     * @param matrix Input/output matrix to be modified
     * @param cosY Pre-computed cos(rotationY) value
     */
    private static void applyYRotation(Matrix matrix, float cosY) {
        // Y-axis rotation is primarily represented as X-direction perspective scaling in 2D projection
        // Directly scale X-direction without matrix copy
        matrix.preScale(cosY, 1f);
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
