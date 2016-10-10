package com.airbnb.lotte.animation;

import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.airbnb.lotte.model.RemapInterface;
import com.airbnb.lotte.utils.JsonUtils;
import com.airbnb.lotte.utils.LotteKeyframeAnimation;
import com.airbnb.lotte.utils.LotteNumberKeyframeAnimation;
import com.airbnb.lotte.utils.Observable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LotteAnimatableNumberValue implements LotteAnimatableValue<Number> {

    private final Observable<Number> observable = new Observable<>();
    private final int frameRate;
    private final long compDuration;
    @Nullable private RemapInterface<Float> remapInterface;
    private float initialValue;

    private final List<Float> valueKeyframes = new ArrayList<>();
    private final List<Float> keyTimes = new ArrayList<>();
    private final List<Interpolator> interpolators = new ArrayList<>();
    private long delay;
    private long duration;
    private long startFrame;
    private long durationFrames;

    @SuppressLint("UseValueOf")
    public LotteAnimatableNumberValue(JSONObject numberValues, int frameRate, long compDuration) {
        this.frameRate = frameRate;
        this.compDuration = compDuration;
        try {
            Object value = numberValues.get("k");
            if (value instanceof JSONArray &&
                    ((JSONArray) value).get(0) instanceof JSONObject &&
                    ((JSONArray) value).getJSONObject(0).has("t")) {
                // Keyframes
                buildAnimationForKeyframes((JSONArray) value);
            } else if (value instanceof Double) {
                // Single value, no animation
                initialValue = new Float((Double) value);
            } else if (value instanceof Integer) {
                initialValue = (Integer) value;
            }
            observable.setValue(initialValue);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse number value " + numberValues, e);
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
            Float outValue = null;

            for (int i = 0; i < keyframes.length(); i++) {
                JSONObject keyframe = keyframes.getJSONObject(i);
                long frame = keyframe.getLong("t");
                float timePercentage = (float) (frame - startFrame) / (float) durationFrames;

                if (outValue != null) {
                    valueKeyframes.add(outValue);
                    interpolators.add(new LinearInterpolator());
                    outValue = null;
                }

                Float startValue = keyframe.has("s") ? numberValueFromObject(keyframe.get("s")) : null;
                if (addStartValue) {
                    if (startValue != null) {
                        if (i == 0) {
                            initialValue = startValue;
                            observable.setValue(initialValue);
                        }
                        valueKeyframes.add(startValue);
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

                Float endValue = keyframe.has("e") ? numberValueFromObject(keyframe.get("e")) : null;
                if (endValue != null) {
                    valueKeyframes.add(endValue);
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

                if (keyframe.has("h") && keyframe.getInt("h") == 1) {
                    outValue = startValue;
                    addStartValue = true;
                    addTimePadding = true;
                }
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse animatable number value.", e);
        }
    }

    @Nullable
    private Float numberValueFromObject(Object valueObject) throws JSONException {
        if (valueObject instanceof Float) {
            return (Float) valueObject;
        } else if (valueObject instanceof JSONArray && ((JSONArray) valueObject).get(0) instanceof Double) {
            return (float) ((JSONArray) valueObject).getDouble(0);
        } else if (valueObject instanceof JSONArray && ((JSONArray) valueObject).get(0) instanceof Integer) {
            return (float) ((JSONArray) valueObject).getInt(0);
        }
        return null;
    }

    public void remapValues(final float fromMin, final float fromMax, final float toMin, final float toMax) {
        remapInterface = new RemapInterface<Float>() {
            @Override
            public Float remap(Float inValue) {
                if (inValue < fromMin) {
                    return toMin;
                } else if (inValue > fromMax) {
                    return toMax;
                } else {
                    return toMin + (inValue / (fromMax - fromMin) * (toMax - toMin));
                }
            }
        };
        observable.setValue(remapInterface.remap((float) observable.getValue()));
    }

    public float getInitialValue() {
        if (remapInterface != null) {
            return remapInterface.remap((float) initialValue);
        }
        return initialValue;
    }


    @Override
    public LotteKeyframeAnimation animationForKeyPath() {
        LotteNumberKeyframeAnimation<Float> animation = new LotteNumberKeyframeAnimation<>(duration, compDuration, keyTimes, Float.class, valueKeyframes, interpolators);
        animation.setStartDelay(delay);
        animation.addUpdateListener(new LotteKeyframeAnimation.AnimationListener<Float>() {
            @Override
            public void onValueChanged(Float progress) {
                observable.setValue(progress);
            }
        });
        return animation;
    }

    @Override
    public boolean hasAnimation() {
        return !valueKeyframes.isEmpty();
    }

    @Override
    public Observable<Number> getObservable() {
        return observable;
    }

    @Override
    public String toString() {
        return "LotteAnimatableNumberValue{" + "initialValue=" + initialValue + '}';
    }
}
