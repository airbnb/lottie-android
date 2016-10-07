package com.airbnb.lotte.model;

import com.airbnb.lotte.animation.LotteAnimatableNumberValue;

import org.json.JSONException;
import org.json.JSONObject;

public class LotteShapeTrimPath {
    private LotteAnimatableNumberValue start;
    private LotteAnimatableNumberValue end;
    private LotteAnimatableNumberValue offset;

    public LotteShapeTrimPath(JSONObject json, int frameRate, long compDuration) {
        try {
            start = new LotteAnimatableNumberValue(json.getJSONObject("s"), frameRate, compDuration);
            end = new LotteAnimatableNumberValue(json.getJSONObject("e"), frameRate, compDuration);
            offset = new LotteAnimatableNumberValue(json.getJSONObject("o"), frameRate, compDuration);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse trim path " + json, e);
        }
    }

    public LotteAnimatableNumberValue getEnd() {
        return end;
    }

    public LotteAnimatableNumberValue getOffset() {
        return offset;
    }

    public LotteAnimatableNumberValue getStart() {
        return start;
    }
}
