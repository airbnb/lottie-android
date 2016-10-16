package com.airbnb.lottie.model;

import android.graphics.Rect;
import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.animation.LottieAnimatableFloatValue;
import com.airbnb.lottie.animation.LottieAnimatableIntegerValue;
import com.airbnb.lottie.animation.LottieAnimatablePathValue;
import com.airbnb.lottie.animation.LottieAnimatablePointValue;
import com.airbnb.lottie.animation.LottieAnimatableScaleValue;
import com.airbnb.lottie.animation.LottieAnimationGroup;

import org.json.JSONException;
import org.json.JSONObject;

public class LottieShapeTransform {
    private static final String TAG = LottieShapeTransform.class.getSimpleName();

    private final Rect compBounds;
    private LottieAnimatablePointValue position;
    private LottieAnimatablePathValue anchor;
    private LottieAnimatableScaleValue scale;
    private LottieAnimatableFloatValue rotation;
    private LottieAnimatableIntegerValue opacity;

    LottieShapeTransform(JSONObject json, int frameRate, long compDuration, Rect compBounds) {
        this.compBounds = compBounds;

        JSONObject jsonPosition;
        try {
            jsonPosition = json.getJSONObject("p");
        } catch (JSONException e) {
            throw new IllegalStateException("Transform has no position.");
        }
        position = new LottieAnimatablePointValue(jsonPosition, frameRate, compDuration);

        JSONObject jsonAnchor;
        try {
            jsonAnchor = json.getJSONObject("a");
        } catch (JSONException e) {
            throw new IllegalStateException("Transform has no anchor.");
        }
        anchor = new LottieAnimatablePathValue(jsonAnchor, frameRate, compDuration);

        JSONObject jsonScale;
        try {
            jsonScale = json.getJSONObject("s");
        } catch (JSONException e) {
            throw new IllegalStateException("Transform has no scale.");
        }
        scale = new LottieAnimatableScaleValue(jsonScale, frameRate, compDuration, false);

        JSONObject jsonRotation;
        try {
            jsonRotation = json.getJSONObject("r");
        } catch (JSONException e) {
            throw new IllegalStateException("Transform has no rotation.");
        }
        rotation = new LottieAnimatableFloatValue(jsonRotation, frameRate, compDuration, false);

        JSONObject jsonOpacity;
        try {
            jsonOpacity = json.getJSONObject("o");
        } catch (JSONException e) {
            throw new IllegalStateException("Transform has no opacity.");
        }
        opacity = new LottieAnimatableIntegerValue(jsonOpacity, frameRate, compDuration, false);
        opacity.remap100To255();

        if (L.DBG) Log.d(TAG, "Parsed new shape transform " + toString());
    }

    public Rect getCompBounds() {
        return compBounds;
    }

    public LottieAnimatablePointValue getPosition() {
        return position;
    }

    public LottieAnimatablePathValue getAnchor() {
        return anchor;
    }

    public LottieAnimatableScaleValue getScale() {
        return scale;
    }

    public LottieAnimatableFloatValue getRotation() {
        return rotation;
    }

    public LottieAnimatableIntegerValue getOpacity() {
        return opacity;
    }

    public LottieAnimationGroup createAnimation() {
        return LottieAnimationGroup.forAnimatableValues(getOpacity(), getPosition(), getAnchor(), getScale(), getRotation());
    }

    @Override
    public String toString() {
        return "LottieShapeTransform{" + "anchor=" + anchor.toString() +
                ", compBounds=" + compBounds +
                ", position=" + position.toString() +
                ", scale=" + scale.toString() +
                ", rotation=" + rotation.getInitialValue() +
                ", opacity=" + opacity.getInitialValue() +
                '}';
    }
}
