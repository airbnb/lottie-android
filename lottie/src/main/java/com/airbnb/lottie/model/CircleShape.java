package com.airbnb.lottie.model;

import com.airbnb.lottie.animatable.AnimatablePathValue;
import com.airbnb.lottie.animatable.AnimatablePointValue;
import com.airbnb.lottie.animatable.AnimationGroup;

import org.json.JSONException;
import org.json.JSONObject;

public class CircleShape {
    private AnimatablePathValue position;
    private AnimatablePointValue size;

    CircleShape(JSONObject json, int frameRate, LottieComposition composition) {
        try {
            position = new AnimatablePathValue(json.getJSONObject("p"), frameRate, composition);
            size = new AnimatablePointValue(json.getJSONObject("s"), frameRate, composition);
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
