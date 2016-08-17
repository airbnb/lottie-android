package com.airbnb.lotte.model;

import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.animation.Interpolator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LotteAnimatablePointValue implements LotteAnimatableValue {

    private final List<Point> pointKeyframes = new ArrayList<>();
    private final List<Float> keyTimes = new ArrayList<>();
    private final List<Interpolator> timingFunctions = new ArrayList<>();

    private boolean usePathAnimation = true;
    private PointF initialPoint;
    private Path animationPath;
    private float delay;
    private float duratoin;
    private int startFrame;
    private int durationFrames;
    private int frameRate;

    public LotteAnimatablePointValue(JSONObject pointValues, int frameRate) {
        usePathAnimation = true;
        this.frameRate = frameRate;

        Object value = null;
        try {
            value = pointValues.get("k");
        } catch (JSONException e) { }
        if (value == null) {
            throw new IllegalArgumentException("Point values have no keyframes.");
        }

        if (value instanceof JSONArray) {
            Object firstObject = null;
            try {
                firstObject = ((JSONArray) value).get(0);
            } catch (JSONException e) {}
            if (firstObject == null) {
                throw new IllegalArgumentException("Unable to parse value.");
            }

            if (firstObject instanceof JSONObject && ((JSONObject) firstObject).has("k")) {
                // Keyframes
                buildAnimationForKeyframes((JSONArray) value);
            } else {
                // Single Value, no animation
                initialPoint = pointFromValueArray((JSONArray) value);
            }
        }
    }

    private void buildAnimationForKeyframes(JSONArray keyframes) {
        // TODO
    }

    private PointF pointFromValueArray(JSONArray values) {
        if (values.length() >= 2) {
            try {
                return new PointF((float) values.getDouble(0), (float) values.getDouble(1));
            } catch (JSONException e) {
                throw new IllegalArgumentException("Unable to parse point for " + values);
            }
        }

        return new PointF();
    }


    public void remapPointsFromBounds(Rect bounds) {
        // TODO
    }

    public void setUsePathAnimation(boolean usePathAnimation) {
        this.usePathAnimation = usePathAnimation;
    }

    public PointF getInitialPoint() {
        return initialPoint;
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
