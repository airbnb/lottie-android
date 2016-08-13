package com.airbnb.lotte.model;

import android.graphics.Rect;

import org.json.JSONObject;

public class LotteAnimatablePointValue {

    private boolean usePathAnimation;

    public LotteAnimatablePointValue(JSONObject pointValues, long frameRate) {
        // TODO
    }

    public void remapPointsFromBounds(Rect bounds) {
        // TODO
    }

    public void setUsePathAnimation(boolean usePathAnimation) {
        this.usePathAnimation = usePathAnimation;
    }
}
