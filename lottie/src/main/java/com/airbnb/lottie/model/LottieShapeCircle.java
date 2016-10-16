package com.airbnb.lottie.model;

import com.airbnb.lottie.animation.LottieAnimatablePathValue;
import com.airbnb.lottie.animation.LottieAnimatablePointValue;
import com.airbnb.lottie.animation.LottieAnimationGroup;

import org.json.JSONException;
import org.json.JSONObject;

public class LottieShapeCircle {
    private LottieAnimatablePathValue position;
    private LottieAnimatablePointValue size;

    LottieShapeCircle(JSONObject json, int frameRate, long compDuration) {
        try {
            position = new LottieAnimatablePathValue(json.getJSONObject("p"), frameRate, compDuration);
            size = new LottieAnimatablePointValue(json.getJSONObject("s"), frameRate, compDuration);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse circle " + json, e);
        }
    }

    public LottieAnimatablePathValue getPosition() {
        return position;
    }

    public LottieAnimatablePointValue getSize() {
        return size;
    }

    public LottieAnimationGroup createAnimation() {
        return LottieAnimationGroup.forAnimatableValues(getPosition(), getSize());
    }
}
