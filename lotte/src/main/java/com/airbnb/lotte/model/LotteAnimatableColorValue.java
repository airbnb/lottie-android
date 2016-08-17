package com.airbnb.lotte.model;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.view.animation.Interpolator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LotteAnimatableColorValue implements LotteAnimatableValue {

    private final List<Integer> colorKeyFrames = new ArrayList<>();
    private final List<Integer> keyTimes = new ArrayList<>();
    private final List<Interpolator> timingFunctions = new ArrayList<>();
    private long delay;
    private long duration;

    private long startFrame;
    private long durationFrames;
    private int frameRate;

    @ColorInt private int initialColor;

    public LotteAnimatableColorValue(JSONObject colorValues, int frameRate) {
        this.frameRate = frameRate;
        try {
            Object value = colorValues.get("k");

            if (value instanceof JSONArray) {
                Object firstObject = ((JSONArray) value).get(0);
                if (firstObject instanceof JSONObject && ((JSONObject) firstObject).has("t")) {
                    // Keyframes
                    buildAnimationForKeyframes((JSONArray) value);
                } else {
                    initialColor = colorValueFromArray((JSONArray) value);
                }
            } else {
                throw new IllegalStateException("Invalid color values.");
            }
        } catch (JSONException e) {
            throw new IllegalStateException("Unable to parse color " + colorValues, e);
        }
    }

    private void buildAnimationForKeyframes(JSONArray keyframes) {

    }

    @ColorInt
    private int colorValueFromArray(JSONArray colorArray) throws JSONException {
        if (colorArray.length() == 4) {
            boolean shouldUse255 = false;
            for (int i = 0; i < colorArray.length(); i++) {
                double colorChannel = colorArray.getDouble(i);
                if (colorChannel <= 1f) {
                    shouldUse255 = true;
                }
            }

            float multiplier = shouldUse255 ? 255f : 1f;
            return Color.argb(
                    (int) (colorArray.getDouble(3) * multiplier),
                    (int) (colorArray.getDouble(0) * multiplier),
                    (int) (colorArray.getDouble(1) * multiplier),
                    (int) (colorArray.getDouble(2) * multiplier));
        }
        return Color.BLACK;
    }


    @Override
    public Object animationForKeyPath(String keyPath) {
        return null;
    }

    @Override
    public boolean hasAnimation() {
        return false;
    }

    @ColorInt
    public int getInitialColor() {
        return initialColor;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LotteAnimatableColorValue{");
        sb.append("initialColor=").append(initialColor);
        sb.append('}');
        return sb.toString();
    }
}
