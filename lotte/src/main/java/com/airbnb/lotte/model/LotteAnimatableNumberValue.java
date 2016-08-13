package com.airbnb.lotte.model;

import android.support.annotation.Nullable;

import org.json.JSONObject;

public class LotteAnimatableNumberValue implements LotteAnimatableValue {

    @Nullable private RemapInterface remapInterface;

    public LotteAnimatableNumberValue(JSONObject numberValues, long frameRate) {
        // TODO
    }

    public void remapValues(float fromMin, float fromMax, float toMin, float toMax) {
        // TODO
    }

    public void remapWith(RemapInterface remapInterface) {
        this.remapInterface = remapInterface;
    }

    public float getInitialValue() {
        // TODO
        return -1f;
    }


    @Override
    public Object animationForKeyPath(String keyPath) {
        return null;
    }

    @Override
    public boolean hasAnimation() {
        return false;
    }
}
