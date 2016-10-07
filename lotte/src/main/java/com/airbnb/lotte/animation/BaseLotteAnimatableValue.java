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
    final List<T> keyValues = new ArrayList<>();
    protected final List<Float> keyTimes = new ArrayList<>();
    final List<Interpolator> interpolators = new ArrayList<>();
    long delay;
    protected long duration;

    private long startFrame;
    private long durationFrames;
    protected int frameRate;
    protected final long compDuration;

    T initialValue;

    BaseLotteAnimatableValue(JSONObject json, int frameRate, long compDuration) {
        this.frameRate = frameRate;
        this.compDuration = compDuration;

        try {
            Object value = json.get("k");

            if (value instanceof JSONArray &&
                    ((JSONArray) value).get(0) instanceof JSONObject &&
                    ((JSONArray) value).getJSONObject(0).has("t")) {
                buildAnimationForKeyframes((JSONArray) value);
            } else {
                initialValue = valueFromObject(value);
                observable.setValue(initialValue);
            }
        } catch (JSONException e) {
            throw new IllegalStateException("Unable to parse json " + json, e);
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
            T outValue = null;

            for (int i = 0; i < keyframes.length(); i++) {
                JSONObject keyframe = keyframes.getJSONObject(i);
                long frame = keyframe.getLong("t");
                float timePercentage = (float) (frame - startFrame) / (float) durationFrames;

                if (outValue != null) {
                    keyValues.add(outValue);
                    interpolators.add(new LinearInterpolator());
                    outValue = null;
                }

                T startValue = keyframe.has("s") ? valueFromObject(keyframe.getJSONArray("s")) : null;
                if (addStartValue) {
                    if (startValue != null) {
                        if (i == 0) {
                            //noinspection ResourceAsColor
                            initialValue = startValue;
                            observable.setValue(initialValue);
                        }
                        keyValues.add(startValue);
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
                    T endValue = valueFromObject(keyframe.getJSONArray("e"));
                    keyValues.add(endValue);
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
                    interpolators.add(timingFunction);
                }

                keyTimes.add(timePercentage);

                if (keyframe.has("h") && keyframe.getBoolean("h")) {
                    outValue = startValue;
                    addStartValue = true;
                    addTimePadding = true;
                }
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse color values.", e);
        }
    }

    public boolean hasAnimation() {
        return !keyValues.isEmpty();
    }

    public T getInitialValue() {
        return initialValue;
    }

    public Observable<T> getObservable() {
        return observable;
    }

    protected abstract T valueFromObject(Object array) throws JSONException;

    public abstract LotteKeyframeAnimation animationForKeyPath();
}
