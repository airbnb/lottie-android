package com.airbnb.lottie.model;

import com.airbnb.lottie.animation.LotteAnimatableFloatValue;

import org.json.JSONException;
import org.json.JSONObject;

public class LotteShapeTrimPath {
    private LotteAnimatableFloatValue start;
    private LotteAnimatableFloatValue end;
    private LotteAnimatableFloatValue offset;

    public LotteShapeTrimPath(JSONObject json, int frameRate, long compDuration) {
        try {
            start = new LotteAnimatableFloatValue(json.getJSONObject("s"), frameRate, compDuration);
            end = new LotteAnimatableFloatValue(json.getJSONObject("e"), frameRate, compDuration);
            offset = new LotteAnimatableFloatValue(json.getJSONObject("o"), frameRate, compDuration);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse trim path " + json, e);
        }
    }

    public LotteAnimatableFloatValue getEnd() {
        return end;
    }

    public LotteAnimatableFloatValue getOffset() {
        return offset;
    }

    public LotteAnimatableFloatValue getStart() {
        return start;
    }
}
