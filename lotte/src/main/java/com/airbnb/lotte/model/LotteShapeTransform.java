package com.airbnb.lotte.model;

import android.graphics.Rect;
import android.util.Log;

import com.airbnb.lotte.L;
import com.airbnb.lotte.animation.LotteAnimatableFloatValue;
import com.airbnb.lotte.animation.LotteAnimatableNumberValue;
import com.airbnb.lotte.animation.LotteAnimatablePointValue;
import com.airbnb.lotte.animation.LotteAnimatableScaleValue;

import org.json.JSONException;
import org.json.JSONObject;

public class LotteShapeTransform {
    private static final String TAG = LotteShapeTransform.class.getSimpleName();

    private final Rect compBounds;
    private LotteAnimatablePointValue position;
    private LotteAnimatablePointValue anchor;
    private LotteAnimatableScaleValue scale;
    private LotteAnimatableFloatValue rotation;
    private LotteAnimatableNumberValue opacity;

    public LotteShapeTransform(JSONObject json, int frameRate, long compDuration, Rect compBounds) {
        this.compBounds = compBounds;

        JSONObject jsonPosition = null;
        try {
            jsonPosition = json.getJSONObject("p");
        } catch (JSONException e) { }
        if (jsonPosition == null) {
            throw new IllegalStateException("Transform has no position.");
        }
        position = new LotteAnimatablePointValue(jsonPosition, frameRate, compDuration);

        JSONObject jsonAnchor = null;
        try {
            jsonAnchor = json.getJSONObject("a");
        } catch (JSONException e) { }
        if (jsonAnchor == null) {
            throw new IllegalStateException("Transform has no anchor.");
        }
        anchor = new LotteAnimatablePointValue(jsonAnchor, frameRate, compDuration);
        anchor.setUsePathAnimation(false);

        JSONObject jsonScale = null;
        try {
            jsonScale = json.getJSONObject("s");
        } catch (JSONException e) { }
        if (jsonScale == null) {
            throw new IllegalStateException("Transform has no scale.");
        }
        scale = new LotteAnimatableScaleValue(jsonScale, frameRate, compDuration);

        JSONObject jsonRotation = null;
        try {
            jsonRotation = json.getJSONObject("r");
        } catch (JSONException e) { }
        if (jsonRotation == null) {
            throw new IllegalStateException("Transform has no rotation.");
        }
        rotation = new LotteAnimatableFloatValue(jsonRotation, frameRate, compDuration);

        JSONObject jsonOpacity = null;
        try {
            jsonOpacity = json.getJSONObject("o");
        } catch (JSONException e) { }
        if (jsonOpacity == null) {
            throw new IllegalStateException("Transform has no opacity.");
        }
        opacity = new LotteAnimatableNumberValue(jsonOpacity, frameRate, compDuration);
        opacity.remapValues(0, 100, 0, 255);

        if (L.DBG) Log.d(TAG, "Parsed new shape transform " + toString());
    }

    public Rect getCompBounds() {
        return compBounds;
    }

    public LotteAnimatablePointValue getPosition() {
        return position;
    }

    public LotteAnimatablePointValue getAnchor() {
        return anchor;
    }

    public LotteAnimatableScaleValue getScale() {
        return scale;
    }

    public LotteAnimatableFloatValue getRotation() {
        return rotation;
    }

    public LotteAnimatableNumberValue getOpacity() {
        return opacity;
    }

    @Override
    public String toString() {
        return "LotteShapeTransform{" + "anchor=" + anchor.getInitialPoint() +
                ", compBounds=" + compBounds +
                ", position=" + position.getInitialPoint() +
                ", scale=" + scale.toString() +
                ", rotation=" + rotation.getInitialValue() +
                ", opacity=" + opacity.getInitialValue() +
                '}';
    }
}
