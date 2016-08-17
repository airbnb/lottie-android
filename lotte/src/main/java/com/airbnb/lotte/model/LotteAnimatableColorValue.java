package com.airbnb.lotte.model;

import android.graphics.Color;
import android.support.annotation.ColorInt;

import org.json.JSONObject;

public class LotteAnimatableColorValue implements LotteAnimatableValue {

    @ColorInt private int initialColor = Color.RED; // TODO

    public LotteAnimatableColorValue(JSONObject colorValues, long frameRate) {
        // TODO
    }


    @Override
    public Object animationForKeyPath(String keyPath) {
        return null;
    }

    @Override
    public boolean hasAnimation() {
        return false;
    }

    @ColorInt
    public int getInitialColor() {
        return initialColor;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LotteAnimatableColorValue{");
        sb.append("initialColor=").append(initialColor);
        sb.append('}');
        return sb.toString();
    }
}
