package com.airbnb.lottie.model;

import com.airbnb.lottie.animation.LotteAnimatablePathValue;
import com.airbnb.lottie.animation.LotteAnimatablePointValue;

import org.json.JSONException;
import org.json.JSONObject;

public class LotteShapeCircle {
    private LotteAnimatablePathValue position;
    private LotteAnimatablePointValue size;

    LotteShapeCircle(JSONObject json, int frameRate, long compDuration) {
        try {
            position = new LotteAnimatablePathValue(json.getJSONObject("p"), frameRate, compDuration);
            size = new LotteAnimatablePointValue(json.getJSONObject("s"), frameRate, compDuration);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse circle " + json, e);
        }
    }

    public LotteAnimatablePathValue getPosition() {
        return position;
    }

    public LotteAnimatablePointValue getSize() {
        return size;
    }
}
