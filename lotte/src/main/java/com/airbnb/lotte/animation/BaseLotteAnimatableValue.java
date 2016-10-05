package com.airbnb.lotte.animation;


import android.graphics.PointF;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.airbnb.lotte.utils.JsonUtils;
import com.airbnb.lotte.utils.LotteKeyframeAnimation;
import com.airbnb.lotte.utils.Observable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseLotteAnimatableValue<T> implements LotteAnimatableValue<T> {

    protected final Observable<T> observable = new Observable<>();
    protected final List<T> keyFrames = new ArrayList<>();
    protected final List<Float> keyTimes = new ArrayList<>();
    protected final List<Interpolator> timingFunctions = new ArrayList<>();
    protected long delay;
    protected long duration;

    protected long startFrame;
    protected long durationFrames;
    protected int frameRate;
    protected final long compDuration;

    protected T initialValue;

    public BaseLotteAnimatableValue(JSONObject json, int frameRate, long compDuration) {
        this.frameRate = frameRate;
        this.compDuration = compDuration;

        try {
            Object value = json.get("k");

            if (value instanceof JSONArray) {
                Object firstObject = ((JSONArray) value).get(0);
                if (firstObject instanceof JSONObject && ((JSONObject) firstObject).has("t")) {
                    // Keyframes
                    buildAnimationForKeyframes((JSONArray) value);
                } else {
                    initialValue = valueFromArray((JSONArray) value);
                    observable.setValue(initialValue);
                }
            } else {
                throw new IllegalStateException("Invalid color values.");
            }
        } catch (JSONException e) {
            throw new IllegalStateException("Unable to parse color " + json, e);
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
                        throw new IllegalStateException("Invalid frame compDuration " + startFrame + "->" + endFrame);
                    }
                    durationFrames = endFrame - startFrame;
                    duration = (long) (durationFrames / (float) frameRate * 1000);
                    delay = (long) (startFrame / (float) frameRate * 1000);
                    break;
                }
            }

            boolean addStartValue = true;
            boolean addTimePadding =  false;
            T outColor = null;

            for (int i = 0; i < keyframes.length(); i++) {
                JSONObject keyframe = keyframes.getJSONObject(i);
                long frame = keyframe.getLong("t");
                float timePercentage = (float) (frame - startFrame) / (float) durationFrames;

                if (outColor != null) {
                    keyFrames.add(outColor);
                    timingFunctions.add(new LinearInterpolator());
                    outColor = null;
                }

                T startColor = keyframe.has("s") ? valueFromArray(keyframe.getJSONArray("s")) : null;
                if (addStartValue) {
                    if (startColor != null) {
                        if (i == 0) {
                            //noinspection ResourceAsColor
                            initialValue = startColor;
                            observable.setValue(initialValue);
                        }
                        keyFrames.add(startColor);
                        if (!timingFunctions.isEmpty()) {
                            timingFunctions.add(new LinearInterpolator());
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
                    T endColor = valueFromArray(keyframe.getJSONArray("e"));
                    keyFrames.add(endColor);
                    /**
                     * Timing function for time interpolation between keyframes.
                     * Should be n - 1 where n is the number of keyframes.
                     */
                    Interpolator timingFunction;
                    if (keyframe.has("o") && keyframe.has("i")) {
                        JSONObject timingControlPoint1 = keyframe.getJSONObject("o");
                        JSONObject timingControlPoint2 = keyframe.getJSONObject("i");
                        PointF cp1 = JsonUtils.pointValueFromDict(timingControlPoint1);
                        PointF cp2 = JsonUtils.pointValueFromDict(timingControlPoint2);

                        timingFunction = PathInterpolatorCompat.create(cp1.x, cp1.y, cp2.x, cp2.y);
                    } else {
                        timingFunction = new LinearInterpolator();
                    }
                    timingFunctions.add(timingFunction);
                }

                keyTimes.add(timePercentage);

                if (keyframe.has("h") && keyframe.getBoolean("h")) {
                    outColor = startColor;
                    addStartValue = true;
                    addTimePadding = true;
                }
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse color values.", e);
        }
    }

    public boolean hasAnimation() {
        return !keyFrames.isEmpty();
    }

    public T getInitialValue() {
        return initialValue;
    }

    public Observable<T> getObservable() {
        return observable;
    }

    protected abstract T valueFromArray(JSONArray array) throws JSONException;

    public abstract LotteKeyframeAnimation animationForKeyPath(@LotteAnimatableProperty.AnimatableProperty int property);
}
