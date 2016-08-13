package com.airbnb.lotte.model;

import org.json.JSONObject;

public class LotteAnimatableColorValue implements LotteAnimatableValue {

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
}
