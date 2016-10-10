package com.airbnb.lottie.utils;

public class LottieTransform3D {
    private float rotationZ;
    private float scaleX = 1f;
    private float scaleY = 1f;

    public LottieTransform3D scale(float sx, float sy) {
        this.scaleX = sx;
        this.scaleY = sy;
        return this;
    }

    public void rotateZ(float rz) {
        rotationZ = rz;
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
        return "LottieTransform3D{" + "scale=" + getScaleX() + "x" + getScaleY() + '}';

    }
}
