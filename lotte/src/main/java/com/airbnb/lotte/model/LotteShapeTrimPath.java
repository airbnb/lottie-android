package com.airbnb.lotte.model;

import org.json.JSONException;
import org.json.JSONObject;

public class LotteShapeTrimPath {
    private static final String TAG = LotteShapeTrimPath.class.getSimpleName();

    private LotteAnimatableNumberValue start;
    private LotteAnimatableNumberValue end;
    private LotteAnimatableNumberValue offset;

    public LotteShapeTrimPath(JSONObject json, int frameRate) {
        try {
            start = new LotteAnimatableNumberValue(json.getJSONObject("s"), frameRate);
            start.remapValues(0, 100, 0, 1);
            end = new LotteAnimatableNumberValue(json.getJSONObject("e"), frameRate);
            end.remapValues(0, 100, 0, 1);
            offset = new LotteAnimatableNumberValue(json.getJSONObject("o"), frameRate);
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
