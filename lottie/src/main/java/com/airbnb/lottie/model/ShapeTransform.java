package com.airbnb.lottie.model;

import android.graphics.Rect;
import android.support.annotation.RestrictTo;
import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.animatable.AnimatableFloatValue;
import com.airbnb.lottie.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.animatable.AnimatablePathValue;
import com.airbnb.lottie.animatable.AnimatableScaleValue;

import org.json.JSONException;
import org.json.JSONObject;

@RestrictTo(RestrictTo.Scope.GROUP_ID)
public class ShapeTransform implements Transform {
    private static final String TAG = ShapeTransform.class.getSimpleName();

    private final Rect compBounds;
    private AnimatablePathValue position;
    private AnimatablePathValue anchor;
    private AnimatableScaleValue scale;
    private AnimatableFloatValue rotation;
    private AnimatableIntegerValue opacity;

    public ShapeTransform(LottieComposition composition) {
        this.compBounds = composition.getBounds();
        this.position = new AnimatablePathValue(composition);
        this.anchor = new AnimatablePathValue(composition);
        this.scale = new AnimatableScaleValue(composition);
        this.rotation = new AnimatableFloatValue(composition, 0f);
        this.opacity = new AnimatableIntegerValue(composition, 255);
    }

    ShapeTransform(JSONObject json, int frameRate, LottieComposition composition) {
        this.compBounds = composition.getBounds();

        JSONObject jsonPosition;
        try {
            jsonPosition = json.getJSONObject("p");
        } catch (JSONException e) {
            throw new IllegalStateException("Transform has no position.");
        }
        position = new AnimatablePathValue(jsonPosition, frameRate, composition);

        JSONObject jsonAnchor;
        try {
            jsonAnchor = json.getJSONObject("a");
        } catch (JSONException e) {
            throw new IllegalStateException("Transform has no anchor.");
        }
        anchor = new AnimatablePathValue(jsonAnchor, frameRate, composition);

        JSONObject jsonScale;
        try {
            jsonScale = json.getJSONObject("s");
        } catch (JSONException e) {
            throw new IllegalStateException("Transform has no scale.");
        }
        scale = new AnimatableScaleValue(jsonScale, frameRate, composition, false);

        JSONObject jsonRotation;
        try {
            jsonRotation = json.getJSONObject("r");
        } catch (JSONException e) {
            throw new IllegalStateException("Transform has no rotation.");
        }
        rotation = new AnimatableFloatValue(jsonRotation, frameRate, composition, false);

        JSONObject jsonOpacity;
        try {
            jsonOpacity = json.getJSONObject("o");
        } catch (JSONException e) {
            throw new IllegalStateException("Transform has no opacity.");
        }
        opacity = new AnimatableIntegerValue(jsonOpacity, frameRate, composition, false, true);

        if (L.DBG) Log.d(TAG, "Parsed new shape transform " + toString());
    }

    public Rect getBounds() {
        return compBounds;
    }

    public AnimatablePathValue getPosition() {
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
