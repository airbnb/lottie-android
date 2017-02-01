package com.airbnb.lottie.utils;

import android.support.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
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

    public boolean isDefault() {
        return scaleX == 1f && scaleY == 1f;
    }

    @Override
    public String toString() {
        return getScaleX() + "x" + getScaleY();
    }
}
