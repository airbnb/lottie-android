package com.airbnb.lotte.model;

import com.airbnb.lotte.animation.LotteAnimatablePointValue;

import org.json.JSONException;
import org.json.JSONObject;

public class LotteShapeCircle {
    private LotteAnimatablePointValue position;
    private LotteAnimatablePointValue size;

    LotteShapeCircle(JSONObject json, int frameRate, long compDuration) {
        try {
            position = new LotteAnimatablePointValue(json.getJSONObject("p"), frameRate, compDuration);
            position.setUsePathAnimation(false);
            size = new LotteAnimatablePointValue(json.getJSONObject("s"), frameRate, compDuration);
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
