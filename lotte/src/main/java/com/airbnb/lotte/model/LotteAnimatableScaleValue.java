package com.airbnb.lotte.model;

import android.graphics.Point;
import android.view.animation.Interpolator;

import com.airbnb.lotte.utils.LotteTransform3D;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"EmptyCatchBlock", "unused"})
public class LotteAnimatableScaleValue implements LotteAnimatableValue {
    private static final String TAG = LotteAnimatableScaleValue.class.getSimpleName();

    private LotteTransform3D initialScale;
    private final List<Point> scaleKeyframes = new ArrayList<>();
    private final List<Float> keyTimes = new ArrayList<>();
    private final List<Interpolator> timingFunctions = new ArrayList<>();

    private float delay;
    private float duration;
    private int startFrame;
    private int durationFrames;
    private int frameRate;


    public LotteAnimatableScaleValue(JSONObject scaleValues, long frameRate) {
        try {
            Object value = scaleValues.get("k");
            if (value instanceof JSONArray) {
                Object firstChild = ((JSONArray) value).get(0);
                if (firstChild instanceof JSONObject && ((JSONObject) firstChild).has("k")) {
                    // Keyframes
                    buildAnimationForKeyframes((JSONArray) value);
                } else {
                    // Single value, no animation.
                    initialScale = xformForValueArray((JSONArray) value);
                }

            } else {
                throw new IllegalStateException("Unknown scale value. " + scaleValues);
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Error parsing scale values.");
        }
    }

    private void buildAnimationForKeyframes(JSONArray keyframes) {
        // TODO
    }

    private LotteTransform3D xformForValueArray(JSONArray value) {
        try {
            if (value.length() >= 2) {
                return new LotteTransform3D().scale((float) value.getDouble(0) / 100f, (float) value.getDouble(1) / 100f, 1);
            }
        } catch (JSONException e) {}

        return new LotteTransform3D();

    }

    public LotteTransform3D getInitialScale() {
        return initialScale;
    }

    @Override
    public Object animationForKeyPath(String keyPath) {
        return null;
    }

    @Override
    public boolean hasAnimation() {
        return false;
    }
}
