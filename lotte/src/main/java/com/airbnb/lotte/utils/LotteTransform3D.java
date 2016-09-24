package com.airbnb.lotte.utils;

public class LotteTransform3D {
    private float rotationZ;
    private float scaleX = 1f;
    private float scaleY = 1f;

    public LotteTransform3D scale(float sx, float sy) {
        this.scaleX = sx;
        this.scaleY = sy;
        return this;
    }

    public LotteTransform3D rotateZ(float rz) {
        rotationZ = rz;
        return this;
    }

    public float getRotationZ() {
        return rotationZ;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LotteTransform3D{");
        sb.append("scale=").append(getScaleX()).append("x").append(getScaleY());
        sb.append('}');
        return sb.toString();

    }
}
