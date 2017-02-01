package com.airbnb.lottie.model;

import android.support.annotation.RestrictTo;

import com.airbnb.lottie.animatable.AnimatableFloatValue;

import org.json.JSONException;
import org.json.JSONObject;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class ShapeTrimPath {
    private final AnimatableFloatValue start;
    private final AnimatableFloatValue end;
    private final AnimatableFloatValue offset;

    ShapeTrimPath(JSONObject json, int frameRate, LottieComposition composition) {
        try {
            start = new AnimatableFloatValue(json.getJSONObject("s"), frameRate, composition, false);
            end = new AnimatableFloatValue(json.getJSONObject("e"), frameRate, composition, false);
            offset = new AnimatableFloatValue(json.getJSONObject("o"), frameRate, composition, false);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse trim path " + json, e);
        }
    }

    public AnimatableFloatValue getEnd() {
        return end;
    }

    public AnimatableFloatValue getStart() {
        return start;
    }

    public AnimatableFloatValue getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return "Trim Path: {start: " + start + ", end: " + end + ", offset: " + offset + "}";
    }
}
