package com.airbnb.lotte.model;

import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;

import org.json.JSONObject;

public class LotteAnimatableNumberValue implements LotteAnimatableValue {

    @Nullable private RemapInterface remapInterface;
    private float initialValue;

    public LotteAnimatableNumberValue(JSONObject numberValues, long frameRate) {
        // TODO
        initialValue = 1f;
    }

    public void remapValues(float fromMin, float fromMax, float toMin, float toMax) {
        // TODO
    }

    public void remapWith(RemapInterface remapInterface) {
        this.remapInterface = remapInterface;
    }

    @FloatRange(from=0f, to=1f)
    public float getInitialValue() {
        // TODO
        return initialValue;
    }


    @Override
    public Object animationForKeyPath(String keyPath) {
        return null;
    }

    @Override
    public boolean hasAnimation() {
        return false;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LotteAnimatableNumberValue{");
        sb.append("initialValue=").append(initialValue);
        sb.append('}');
        return sb.toString();
    }
}
