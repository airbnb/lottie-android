package com.airbnb.lottie.model;

import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.animation.AnimatableFloatValue;
import com.airbnb.lottie.animation.AnimatablePointValue;
import com.airbnb.lottie.animation.AnimatablePathValue;
import com.airbnb.lottie.animation.AnimationGroup;

import org.json.JSONException;
import org.json.JSONObject;

public class LottieShapeRectangle {
    private static final String TAG = LottieShapeRectangle.class.getSimpleName();

    private AnimatablePathValue position;
    private AnimatablePointValue size;
    private AnimatableFloatValue cornerRadius;

    LottieShapeRectangle(JSONObject json, int frameRate, long compDuration) {
        try {
            JSONObject positionJson = json.getJSONObject("p");
            position = new AnimatablePathValue(positionJson, frameRate, compDuration);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse rectangle position.", e);
        }

        try {
            JSONObject cornerRadiusJson = json.getJSONObject("r");
            cornerRadius = new AnimatableFloatValue(cornerRadiusJson, frameRate, compDuration);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse rectangle corner radius.", e);
        }

        try {
            JSONObject sizeJson = json.getJSONObject("s");
            size = new AnimatablePointValue(sizeJson, frameRate, compDuration);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse rectangle size.", e);
        }

        if (L.DBG) Log.d(TAG, "Parsed new rectangle " + toString());
    }

    public AnimatableFloatValue getCornerRadius() {
        return cornerRadius;
    }

    public AnimatablePointValue getSize() {
        return size;
    }

    public AnimatablePathValue getPosition() {
        return position;
    }

    public AnimationGroup createAnimation() {
        return AnimationGroup.forAnimatableValues(getCornerRadius(), getSize(), getPosition());
    }

    @Override
    public String toString() {
        return "LottieShapeRectangle{" + "cornerRadius=" + cornerRadius.getInitialValue() +
                ", position=" + position +
                ", size=" + size +
                '}';
    }
}
