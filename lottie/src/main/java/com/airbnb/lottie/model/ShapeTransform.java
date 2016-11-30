package com.airbnb.lottie.model;

import android.graphics.Rect;
import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.animatable.AnimatableFloatValue;
import com.airbnb.lottie.animatable.AnimatablePathValue;
import com.airbnb.lottie.animatable.AnimatablePointValue;
import com.airbnb.lottie.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.animatable.AnimatableScaleValue;
import com.airbnb.lottie.animatable.AnimationGroup;

import org.json.JSONException;
import org.json.JSONObject;

public class ShapeTransform {
    private static final String TAG = ShapeTransform.class.getSimpleName();

    private final Rect compBounds;
    private AnimatablePointValue position;
    private AnimatablePathValue anchor;
    private AnimatableScaleValue scale;
    private AnimatableFloatValue rotation;
    private AnimatableIntegerValue opacity;

    ShapeTransform(JSONObject json, int frameRate, long compDuration, Rect compBounds) {
        this.compBounds = compBounds;

        JSONObject jsonPosition;
        try {
            jsonPosition = json.getJSONObject("p");
        } catch (JSONException e) {
            throw new IllegalStateException("Transform has no position.");
        }
        position = new AnimatablePointValue(jsonPosition, frameRate, compDuration);

        JSONObject jsonAnchor;
        try {
            jsonAnchor = json.getJSONObject("a");
        } catch (JSONException e) {
            throw new IllegalStateException("Transform has no anchor.");
        }
        anchor = new AnimatablePathValue(jsonAnchor, frameRate, compDuration);

        JSONObject jsonScale;
        try {
            jsonScale = json.getJSONObject("s");
        } catch (JSONException e) {
            throw new IllegalStateException("Transform has no scale.");
        }
        scale = new AnimatableScaleValue(jsonScale, frameRate, compDuration, false);

        JSONObject jsonRotation;
        try {
            jsonRotation = json.getJSONObject("r");
        } catch (JSONException e) {
            throw new IllegalStateException("Transform has no rotation.");
        }
        rotation = new AnimatableFloatValue(jsonRotation, frameRate, compDuration, false);

        JSONObject jsonOpacity;
        try {
            jsonOpacity = json.getJSONObject("o");
        } catch (JSONException e) {
            throw new IllegalStateException("Transform has no opacity.");
        }
        opacity = new AnimatableIntegerValue(jsonOpacity, frameRate, compDuration, false, true);

        if (L.DBG) Log.d(TAG, "Parsed new shape transform " + toString());
    }

    public Rect getCompBounds() {
        return compBounds;
    }

    public AnimatablePointValue getPosition() {
        return position;
    }

    public AnimatablePathValue getAnchor() {
        return anchor;
    }

    public AnimatableScaleValue getScale() {
        return scale;
    }

    public AnimatableFloatValue getRotation() {
        return rotation;
    }

    public AnimatableIntegerValue getOpacity() {
        return opacity;
    }

    public AnimationGroup createAnimation() {
        return AnimationGroup.forAnimatableValues(getOpacity(), getPosition(), getAnchor(), getScale(), getRotation());
    }

    @Override
    public String toString() {
        return "ShapeTransform{" + "anchor=" + anchor.toString() +
                ", compBounds=" + compBounds +
                ", position=" + position.toString() +
                ", scale=" + scale.toString() +
                ", rotation=" + rotation.getInitialValue() +
                ", opacity=" + opacity.getInitialValue() +
                '}';
    }
}
