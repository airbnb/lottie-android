package com.airbnb.lotte.model;

import android.graphics.Rect;

import org.json.JSONException;
import org.json.JSONObject;

public class LotteShapeTransform {

    private final Rect compBounds;
    private LotteAnimatablePointValue position;
    private LotteAnimatablePointValue anchor;
    private LotteAnimatableScaleValue scale;
    private LotteAnimatableNumberValue rotation;
    private LotteAnimatableNumberValue opacity;

    public LotteShapeTransform(JSONObject json, int frameRate, Rect compBounds) {
        // TODO
        this.compBounds = compBounds;

        JSONObject jsonPosition = null;
        try {
            jsonPosition = json.getJSONObject("p");
        } catch (JSONException e) { }
        if (jsonPosition == null) {
            throw new IllegalStateException("Transform has no position.");
        }
        position = new LotteAnimatablePointValue(jsonPosition, frameRate);

        JSONObject jsonAnchor = null;
        try {
            jsonAnchor = json.getJSONObject("a");
        } catch (JSONException e) { }
        if (jsonAnchor == null) {
            throw new IllegalStateException("Transform has no anchor.");
        }
        anchor = new LotteAnimatablePointValue(jsonAnchor, frameRate);
        anchor.remapPointsFromBounds(new Rect(0, 0, 1, 1));
        anchor.setUsePathAnimation(false);

        JSONObject jsonScale = null;
        try {
            jsonScale = json.getJSONObject("s");
        } catch (JSONException e) { }
        if (jsonScale == null) {
            throw new IllegalStateException("Transform has no scale.");
        }
        scale = new LotteAnimatableScaleValue(jsonScale, frameRate);

        JSONObject jsonRotation = null;
        try {
            jsonRotation = json.getJSONObject("r");
        } catch (JSONException e) { }
        if (jsonRotation == null) {
            throw new IllegalStateException("Transform has no rotation.");
        }
        rotation = new LotteAnimatableNumberValue(jsonRotation, frameRate);
        rotation.remapWith(new RemapInterface() {
            @Override
            public float remap(float inValue) {
                return (float) Math.toRadians(inValue);
            }
        });

        JSONObject jsonOpacity = null;
        try {
            jsonOpacity = json.getJSONObject("o");
        } catch (JSONException e) { }
        if (jsonOpacity == null) {
            throw new IllegalStateException("Transform has no opacity.");
        }
        opacity = new LotteAnimatableNumberValue(jsonOpacity, frameRate);
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

    public LotteAnimatableNumberValue getRotation() {
        return rotation;
    }

    public LotteAnimatableNumberValue getOpacity() {
        return opacity;
    }
}
