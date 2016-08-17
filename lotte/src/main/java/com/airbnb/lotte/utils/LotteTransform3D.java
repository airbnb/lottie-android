package com.airbnb.lotte.utils;

import android.opengl.Matrix;

public class LotteTransform3D {
    private static final int IDX_SX = 0;
    private static final int IDX_SY = 5;

    private final float[] matrix = new float[16];

    public LotteTransform3D() {
        Matrix.setIdentityM(matrix, 0);
    }

    public LotteTransform3D scale(float sx, float sy, float sz) {
        Matrix.scaleM(matrix, 0, sx, sy, sz);
        return this;
    }

    public LotteTransform3D rotateZ(float rz) {
        Matrix.rotateM(matrix, 0, rz, 0, 0, 1);
        return this;
    }

    public float getScaleX() {
        return matrix[IDX_SX];
    }

    public float getScaleY() {
        return matrix[IDX_SY];
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LotteTransform3D{");
        sb.append("scale=").append(getScaleX()).append("x").append(getScaleY());
        sb.append('}');
        return sb.toString();
    }
}
