package com.airbnb.lotte.model;

import android.graphics.PointF;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.airbnb.lotte.utils.JsonUtils;
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
    private final List<LotteTransform3D> scaleKeyframes = new ArrayList<>();
    private final List<Float> keyTimes = new ArrayList<>();
    private final List<Interpolator> interpolators = new ArrayList<>();

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
        try {
            startFrame = keyframes.getJSONObject(0).getInt("t");
            int endFrame = keyframes.getJSONObject(keyframes.length() - 1).getInt("t");

            if (endFrame <= startFrame) {
                throw new IllegalArgumentException("End frame must be after start frame " + endFrame + " vs " + startFrame);
            }

            durationFrames = endFrame - startFrame;

            duration = durationFrames / frameRate;
            delay = startFrame / frameRate;

            boolean addStartValue = true;
            boolean addTimePadding = false;
            LotteTransform3D outValue = null;

            for (int i = 0; i < keyframes.length(); i++) {
                JSONObject keyframe = keyframes.getJSONObject(i);
                int frame = keyframe.getInt("t");
                float timePercentage = (frame - startFrame) / (float) durationFrames;

                if (outValue != null) {
                    scaleKeyframes.add(outValue);
                    interpolators.add(new LinearInterpolator());
                    outValue = null;
                }

                LotteTransform3D startValue = null;
                if (addStartValue) {
                    if (keyframe.has("s")) {
                        startValue = xformForValueArray(keyframe.getJSONArray("s"));
                        if (i == 0) {
                            initialScale = startValue;
                        }
                        scaleKeyframes.add(startValue);
                        if (!interpolators.isEmpty()) {
                            interpolators.add(new LinearInterpolator());
                        }
                    }
                    addStartValue = false;
                }

                if (addTimePadding) {
                    float holdPercentage = timePercentage - 0.00001f;
                    keyTimes.add(holdPercentage);
                    addTimePadding = false;
                }

                if (keyframe.has("e")) {
                    LotteTransform3D endValue = xformForValueArray(keyframe.getJSONArray("e"));
                    scaleKeyframes.add(endValue);

                    Interpolator interpolator;

                    if (keyframe.has("o") && keyframe.has("i")) {
                        PointF cp1 = JsonUtils.pointValueFromDict(keyframe.getJSONObject("o"));
                        PointF cp2 = JsonUtils.pointValueFromDict(keyframe.getJSONObject("i"));
                        interpolator = PathInterpolatorCompat.create(cp1.x, cp1.y, cp2.x, cp2.y);
                    } else {
                        interpolator = new LinearInterpolator();
                    }
                    interpolators.add(interpolator);
                }

                keyTimes.add(timePercentage);

                if (keyframe.has("h") && keyframe.getBoolean("h")) {
                    outValue = startValue;
                    addStartValue = true;
                    addTimePadding = true;
                }
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse scale animation " + keyframes, e);
        }
    }

    private LotteTransform3D xformForValueArray(JSONArray value) {
        try {
            if (value.length() >= 2) {
                return new LotteTransform3D().scale((float) value.getDouble(0) / 100f, (float) value.getDouble(1) / 100f);
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LotteAnimatableScaleValue{");
        sb.append("initialScale=").append(initialScale);
        sb.append('}');
        return sb.toString();
    }
}
