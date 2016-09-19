package com.airbnb.lotte.model;

import com.airbnb.lotte.animation.LotteAnimatablePointValue;

import org.json.JSONException;
import org.json.JSONObject;

public class LotteShapeCircle {
    private static final String TAG = LotteShapeCircle.class.getSimpleName();

    private LotteAnimatablePointValue position;
    private LotteAnimatablePointValue size;

    public LotteShapeCircle(JSONObject json, int frameRate) {
        try {
            position = new LotteAnimatablePointValue(json.getJSONObject("p"), frameRate);
            position.setUsePathAnimation(false);
            size = new LotteAnimatablePointValue(json.getJSONObject("s"), frameRate);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse circle " + json, e);
        }
    }

    public LotteAnimatablePointValue getPosition() {
        return position;
    }

    public LotteAnimatablePointValue getSize() {
        return size;
    }
}
