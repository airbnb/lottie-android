package com.airbnb.lotte.model;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class LotteAnimatableNumberValue implements LotteAnimatableValue {

    private final int frameRate;
    @Nullable private RemapInterface remapInterface;
    private float initialValue;

    public LotteAnimatableNumberValue(JSONObject numberValues, int frameRate) {
        this.frameRate = frameRate;
        try {
            Object value = numberValues.get("k");
            if (value instanceof JSONArray &&
                    ((JSONArray) value).get(0) instanceof JSONObject &&
                    ((JSONArray) value).getJSONObject(0).has("t")) {
                // Keyframes
                buildAnimationForKeyframes((JSONArray) value);
            } else if (value instanceof Double) {
                // Single value, no animation
                initialValue = (float) value;
            } else if (value instanceof Integer) {
                initialValue = (Integer) value;
            }

        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse number value " + numberValues, e);
        }
    }

    private void buildAnimationForKeyframes(JSONArray keyframes) {
        // TODO
    }

    public void remapValues(float fromMin, float fromMax, float toMin, float toMax) {
        // TODO
    }

    public void remapWith(RemapInterface remapInterface) {
        this.remapInterface = remapInterface;
    }

    public float getInitialValue() {
        return initialValue;
    }


    @Override
    public Object animationForKeyPath(String keyPath) {
        return null;
    }

    @Override
    public boolean hasAnimation() {
        return false;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LotteAnimatableNumberValue{");
        sb.append("initialValue=").append(initialValue);
        sb.append('}');
        return sb.toString();
    }
}
