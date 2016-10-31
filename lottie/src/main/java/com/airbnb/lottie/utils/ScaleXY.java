package com.airbnb.lottie.utils;

public class ScaleXY {
    private float scaleX = 1f;
    private float scaleY = 1f;

    public ScaleXY scale(float sx, float sy) {
        this.scaleX = sx;
        this.scaleY = sy;
        return this;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    @Override
    public String toString() {
        return "ScaleXY{" + "scale=" + getScaleX() + "x" + getScaleY() + '}';

    }
}
