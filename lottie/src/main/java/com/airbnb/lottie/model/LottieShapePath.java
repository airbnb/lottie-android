package com.airbnb.lottie.model;

import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.animation.LottieAnimatableShapeValue;
import com.airbnb.lottie.animation.LottieAnimationGroup;

import org.json.JSONException;
import org.json.JSONObject;

public class LottieShapePath {
    private static final String TAG = LottieShapePath.class.getSimpleName();

    private String name;
    private boolean closed;
    private int index;
    private LottieAnimatableShapeValue shapePath;

    public LottieShapePath(JSONObject json, int frameRate, long compDuration) {
        try {
            index = json.getInt("ind");
        } catch (JSONException e) {
            throw new IllegalArgumentException("ShapePath has no index.", e);
        }

        try {
            name = json.getString("nm");
        } catch (JSONException e) {
            throw new IllegalArgumentException("Layer has no name.", e);
        }

        try {
            closed = json.getBoolean("closed");
        } catch (JSONException e) {
            throw new IllegalArgumentException("ShapePath index " + index + " has no value for 'closed'.", e);
        }

        JSONObject shape;
        try {
            shape = json.getJSONObject("ks");
            shapePath = new LottieAnimatableShapeValue(shape, frameRate, compDuration, closed);
        } catch (JSONException e) {
            // Ignore
        }

        if (L.DBG) Log.d(TAG, "Parsed new shape path " + toString());
    }

    public LottieAnimatableShapeValue getShapePath() {
        return shapePath;
    }

    public LottieAnimationGroup createAnimation() {
        return LottieAnimationGroup.forAnimatableValues(getShapePath());
    }

    @Override
    public String toString() {
        return "LottieShapePath{" + "name=" + name +
                ", closed=" + closed +
                ", index=" + index +
                ", hasAnimation=" + shapePath.hasAnimation() +
                '}';
    }
}
