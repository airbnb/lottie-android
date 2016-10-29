package com.airbnb.lottie.model;

import com.airbnb.lottie.animation.AnimatablePathValue;
import com.airbnb.lottie.animation.AnimatablePointValue;
import com.airbnb.lottie.animation.AnimationGroup;

import org.json.JSONException;
import org.json.JSONObject;

public class LottieShapeCircle {
    private AnimatablePathValue position;
    private AnimatablePointValue size;

    LottieShapeCircle(JSONObject json, int frameRate, long compDuration) {
        try {
            position = new AnimatablePathValue(json.getJSONObject("p"), frameRate, compDuration);
            size = new AnimatablePointValue(json.getJSONObject("s"), frameRate, compDuration);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse circle " + json, e);
        }
    }

    public AnimatablePathValue getPosition() {
        return position;
    }

    public AnimatablePointValue getSize() {
        return size;
    }

    public AnimationGroup createAnimation() {
        return AnimationGroup.forAnimatableValues(getPosition(), getSize());
    }
}
