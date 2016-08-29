package com.airbnb.lotte.model;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LotteAnimatablePointValue implements LotteAnimatableValue {
    private static final String TAG = LotteAnimatablePointValue.class.getSimpleName();

    private final List<PointF> pointKeyframes = new ArrayList<>();
    private final List<Float> keyTimes = new ArrayList<>();
    private final List<Interpolator> timingFunctions = new ArrayList<>();

    private boolean usePathAnimation = true;
    private PointF initialPoint;
    private Path animationPath;
    private float delayMs;
    private float durationMs;
    private long startFrame;
    private long durationFrames;
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
            } catch (JSONException e) { }
            if (firstObject == null) {
                throw new IllegalArgumentException("Unable to parse value.");
            }

            if (firstObject instanceof JSONObject && ((JSONObject) firstObject).has("t")) {
                // Keyframes
                buildAnimationForKeyframes((JSONArray) value);
            } else {
                // Single Value, no animation
                initialPoint = pointFromValueArray((JSONArray) value);
            }
        }
    }

    private void buildAnimationForKeyframes(JSONArray keyframes) {
        try {
            for (int i = 0; i < keyframes.length(); i++) {
                JSONObject kf = keyframes.getJSONObject(i);
                if (kf.has("t")) {
                    startFrame = kf.getLong("t");
                    break;
                }
            }

            for (int i = keyframes.length() - 1; i >= 0; i--) {
                JSONObject keyframe = keyframes.getJSONObject(i);
                if (keyframe.has("t")) {
                    long endFrame = keyframe.getLong("t");
                    if (endFrame <= startFrame) {
                        throw new IllegalStateException("Invalid frame duration " + startFrame + "->" + endFrame);
                    }
                    durationFrames = endFrame - startFrame;
                    durationMs = durationFrames / frameRate;
                    delayMs = startFrame / frameRate;
                    break;
                }
            }

            boolean addStartValue = true;
            boolean addTimePadding =  false;
            JSONArray outPoint = null;

            for (int i = 0; i < keyframes.length(); i++) {
                JSONObject keyframe = keyframes.getJSONObject(i);
                long frame = keyframe.getLong("t");
                float timePercentage = (frame - startFrame) / durationFrames;

                if (outPoint != null) {
                    PointF vertex = pointFromValueArray(outPoint);
                    animationPath.lineTo(vertex.x, vertex.y);
                    pointKeyframes.add(vertex);
                    timingFunctions.add(new LinearInterpolator());
                    outPoint = null;
                }

                PointF startPoint = pointFromValueArray(keyframe.getJSONArray("s"));
                if (addStartValue) {
                    if (i == 0) {
                        pointKeyframes.add(startPoint);
                        animationPath.moveTo(startPoint.x, startPoint.y);
                        initialPoint = startPoint;
                    } else {
                        animationPath.lineTo(startPoint.x, startPoint.y);
                        pointKeyframes.add(startPoint);
                        timingFunctions.add(new LinearInterpolator());
                    }
                    addStartValue = false;
                }

                if (addTimePadding) {
                    float holdPercentage = timePercentage - 0.00001f;
                    keyTimes.add(holdPercentage);
                    addTimePadding = false;
                }

                if (keyframe.has("e")) {
                    PointF vertex = pointFromValueArray(keyframe.getJSONArray("e"));

                    Interpolator timingFunction;
                    if (keyframe.has("o") && keyframe.has("i")) {
                        PointF cp1 = pointFromValueArray(keyframe.getJSONArray("to"));
                        PointF cp2 = pointFromValueArray(keyframe.getJSONArray("ti"));
                        // TODO: inVertex
//                        animationPath.cubicTo(inVer);

                        timingFunction = PathInterpolatorCompat.create(cp1.x, cp1.y, cp2.x, cp2.y);
                    } else {
                        timingFunction = new LinearInterpolator();
                    }
                    timingFunctions.add(timingFunction);
                }

                keyTimes.add(timePercentage);

                if (keyframe.has("h") && keyframe.getBoolean("h")) {
//                    outPoint = startValue;
                    addStartValue = true;
                    addTimePadding = true;
                }
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse keyframes " + keyframes, e);
        }
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LotteAnimatablePointValue{");
        sb.append("initialPoint=").append(initialPoint);
        sb.append('}');
        return sb.toString();
    }
}
