package com.airbnb.lottie.model;

import com.airbnb.lottie.animation.LottieAnimatableFloatValue;

import org.json.JSONException;
import org.json.JSONObject;

public class LottieShapeTrimPath {
    private LottieAnimatableFloatValue start;
    private LottieAnimatableFloatValue end;
    private LottieAnimatableFloatValue offset;

    public LottieShapeTrimPath(JSONObject json, int frameRate, long compDuration) {
        try {
            start = new LottieAnimatableFloatValue(json.getJSONObject("s"), frameRate, compDuration);
            end = new LottieAnimatableFloatValue(json.getJSONObject("e"), frameRate, compDuration);
            offset = new LottieAnimatableFloatValue(json.getJSONObject("o"), frameRate, compDuration);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse trim path " + json, e);
        }
    }

    public LottieAnimatableFloatValue getEnd() {
        return end;
    }

    public LottieAnimatableFloatValue getOffset() {
        return offset;
    }

    public LottieAnimatableFloatValue getStart() {
        return start;
    }
}
