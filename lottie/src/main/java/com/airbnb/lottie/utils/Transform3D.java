package com.airbnb.lottie.utils;

import android.graphics.Matrix;
import android.graphics.PointF;

/**
 * 3D transformation utility class that mimics iOS CATransform3D implementation
 * Provides more accurate 3D rotation calculations, especially for Z-axis rotation
 */
public class Transform3D {

    /**
     * Creates a 3D transformation matrix following Lottie Android's transformation order
     *
     * @param anchor Anchor point
     * @param position Position
     * @param scaleX X-axis scale
     * @param scaleY Y-axis scale
     * @param rotationX X-axis rotation (degrees)
     * @param rotationY Y-axis rotation (degrees)
     * @param rotationZ Z-axis rotation (degrees)
     * @return Calculated Matrix
     */
    public static Matrix makeTransform(
            PointF anchor, 
            PointF position,
            float scaleX, 
            float scaleY,
            float rotationX, 
            float rotationY, 
            float rotationZ) {
        
        Matrix matrix = new Matrix();
        
        // Follow original Lottie Android order: position → rotation → scale → anchor

        // 1. Apply position transformation  
        if (position != null && (position.x != 0 || position.y != 0)) {
            matrix.preTranslate(position.x, position.y);
        }
        
        // 2. Apply 3D rotation (rotate directly without anchor, as anchor is handled at the end)
        if (rotationZ != 0) {
            matrix.preRotate(rotationZ);
        }
        if (rotationY != 0) {
            matrix = applyYRotation(matrix, rotationY);
        }
        if (rotationX != 0) {
            matrix = applyXRotation(matrix, rotationX);
        }
        
        // 3. Apply scale (Note: Lottie's scale doesn't need to be divided by 100, use ScaleXY values directly)
        if (scaleX != 1.0f || scaleY != 1.0f) {
            matrix.preScale(scaleX, scaleY);
        }
        
        // 4. Finally translate to negative anchor position (consistent with original code)
        if (anchor != null && (anchor.x != 0 || anchor.y != 0)) {
            matrix.preTranslate(-anchor.x, -anchor.y);
        }
        
        return matrix;
    }
    
    
    /**
     * Apply X-axis rotation
     * On a 2D plane, X-axis rotation primarily affects Y-direction scaling
     */
    private static Matrix applyXRotation(Matrix matrix, float degrees) {
        // X-axis rotation is primarily represented as Y-direction perspective scaling in 2D projection
        float radians = (float) Math.toRadians(degrees);
        float cosX = (float) Math.cos(radians);
        
        Matrix result = new Matrix(matrix);
        
        // Apply Y-direction scale transformation (projection of X-axis rotation)
        result.preScale(1f, cosX);
        
        return result;
    }
    
    /**
     * Apply Y-axis rotation
     * On a 2D plane, Y-axis rotation primarily affects X-direction scaling
     */
    private static Matrix applyYRotation(Matrix matrix, float degrees) {
        // Y-axis rotation is primarily represented as X-direction perspective scaling in 2D projection
        float radians = (float) Math.toRadians(degrees);
        float cosY = (float) Math.cos(radians);
        
        Matrix result = new Matrix(matrix);
        
        // Apply X-direction scale transformation (projection of Y-axis rotation)
        result.preScale(cosY, 1f);
        
        return result;
    }
    
    /**
     * Apply Z-axis rotation
     * Z-axis rotation is standard 2D rotation
     */
    private static Matrix applyZRotation(Matrix matrix, float degrees) {
        Matrix result = new Matrix(matrix);
        result.preRotate(degrees);
        return result;
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
