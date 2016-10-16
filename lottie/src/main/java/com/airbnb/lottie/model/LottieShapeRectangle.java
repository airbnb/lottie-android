package com.airbnb.lottie.model;

import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.animation.LottieAnimatableFloatValue;
import com.airbnb.lottie.animation.LottieAnimatablePathValue;
import com.airbnb.lottie.animation.LottieAnimatablePointValue;
import com.airbnb.lottie.animation.LottieAnimationGroup;

import org.json.JSONException;
import org.json.JSONObject;

public class LottieShapeRectangle {
    private static final String TAG = LottieShapeRectangle.class.getSimpleName();

    private LottieAnimatablePathValue position;
    private LottieAnimatablePointValue size;
    private LottieAnimatableFloatValue cornerRadius;

    LottieShapeRectangle(JSONObject json, int frameRate, long compDuration) {
        try {
            JSONObject positionJson = json.getJSONObject("p");
            position = new LottieAnimatablePathValue(positionJson, frameRate, compDuration);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse rectangle position.", e);
        }

        try {
            JSONObject cornerRadiusJson = json.getJSONObject("r");
            cornerRadius = new LottieAnimatableFloatValue(cornerRadiusJson, frameRate, compDuration);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse rectangle corner radius.", e);
        }

        try {
            JSONObject sizeJson = json.getJSONObject("s");
            size = new LottieAnimatablePointValue(sizeJson, frameRate, compDuration);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse rectangle size.", e);
        }

        if (L.DBG) Log.d(TAG, "Parsed new rectangle " + toString());
    }

    public LottieAnimatableFloatValue getCornerRadius() {
        return cornerRadius;
    }

    public LottieAnimatablePointValue getSize() {
        return size;
    }

    public LottieAnimatablePathValue getPosition() {
        return position;
    }

    public LottieAnimationGroup createAnimation() {
        return LottieAnimationGroup.forAnimatableValues(getCornerRadius(), getSize(), getPosition());
    }

    @Override
    public String toString() {
        return "LottieShapeRectangle{" + "cornerRadius=" + cornerRadius.getInitialValue() +
                ", position=" + position +
                ", size=" + size +
                '}';
    }
}
