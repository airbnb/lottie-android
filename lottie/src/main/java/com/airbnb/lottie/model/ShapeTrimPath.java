package com.airbnb.lottie.model;

import com.airbnb.lottie.animatable.AnimatableFloatValue;
import com.airbnb.lottie.animatable.AnimationGroup;

import org.json.JSONException;
import org.json.JSONObject;

public class ShapeTrimPath {
    private AnimatableFloatValue start;
    private AnimatableFloatValue end;
    private AnimatableFloatValue offset;

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

    private AnimatableFloatValue getOffset() {
        return offset;
    }

    public AnimatableFloatValue getStart() {
        return start;
    }

    public AnimationGroup createAnimation() {
        return AnimationGroup.forAnimatableValues(getStart(), getEnd(), getOffset());
    }
}
