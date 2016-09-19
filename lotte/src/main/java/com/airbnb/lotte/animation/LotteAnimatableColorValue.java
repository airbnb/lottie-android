package com.airbnb.lotte.animation;

import android.graphics.Color;
import android.graphics.PointF;
import android.support.annotation.ColorInt;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.airbnb.lotte.animation.LotteAnimatableProperty.AnimatableProperty;
import com.airbnb.lotte.utils.JsonUtils;
import com.airbnb.lotte.utils.LotteColorKeyframeAnimation;
import com.airbnb.lotte.utils.LotteKeyframeAnimation;
import com.airbnb.lotte.utils.Observable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LotteAnimatableColorValue implements LotteAnimatableValue<Integer> {

    private final Observable<Integer> observable = new Observable<>();
    private final List<Integer> colorKeyframes = new ArrayList<>();
    private final List<Float> keyTimes = new ArrayList<>();
    private final List<Interpolator> timingFunctions = new ArrayList<>();
    private long delay;
    private long duration;

    private long startFrame;
    private long durationFrames;
    private int frameRate;

    @ColorInt private int initialColor;

    public LotteAnimatableColorValue(JSONObject colorValues, int frameRate) {
        this.frameRate = frameRate;
        try {
            Object value = colorValues.get("k");

            if (value instanceof JSONArray) {
                Object firstObject = ((JSONArray) value).get(0);
                if (firstObject instanceof JSONObject && ((JSONObject) firstObject).has("t")) {
                    // Keyframes
                    buildAnimationForKeyframes((JSONArray) value);
                } else {
                    initialColor = colorValueFromArray((JSONArray) value);
                    observable.setValue(initialColor);
                }
            } else {
                throw new IllegalStateException("Invalid color values.");
            }
        } catch (JSONException e) {
            throw new IllegalStateException("Unable to parse color " + colorValues, e);
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
                    duration = durationFrames / frameRate * 1000;
                    delay = startFrame / frameRate * 1000;
                    break;
                }
            }

            boolean addStartValue = true;
            boolean addTimePadding =  false;
            Integer outColor = null;

            for (int i = 0; i < keyframes.length(); i++) {
                JSONObject keyframe = keyframes.getJSONObject(i);
                long frame = keyframe.getLong("t");
                float timePercentage = (frame - startFrame) / durationFrames;

                if (outColor != null) {
                    colorKeyframes.add(outColor);
                    timingFunctions.add(new LinearInterpolator());
                    outColor = null;
                }

                Integer startColor = colorValueFromArray(keyframe.getJSONArray("s"));
                if (addStartValue) {
                    if (i == 0) {
                        initialColor = startColor;
                    }
                    colorKeyframes.add(startColor);
                    if (!timingFunctions.isEmpty()) {
                        timingFunctions.add(new LinearInterpolator());
                    }
                    addStartValue = false;
                }

                if (addTimePadding) {
                    float holdPercentage = timePercentage - 0.00001f;
                    keyTimes.add(holdPercentage);
                    addTimePadding = false;
                }

                Integer endColor = colorValueFromArray(keyframe.getJSONArray("e"));
                colorKeyframes.add(endColor);
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


    @ColorInt
    private int colorValueFromArray(JSONArray colorArray) throws JSONException {
        if (colorArray.length() == 4) {
            boolean shouldUse255 = true;
            for (int i = 0; i < colorArray.length(); i++) {
                double colorChannel = colorArray.getDouble(i);
                if (colorChannel > 1f) {
                    shouldUse255 = false;
                }
            }

            float multiplier = shouldUse255 ? 255f : 1f;
            return Color.argb(
                    (int) (colorArray.getDouble(3) * multiplier),
                    (int) (colorArray.getDouble(0) * multiplier),
                    (int) (colorArray.getDouble(1) * multiplier),
                    (int) (colorArray.getDouble(2) * multiplier));
        }
        return Color.BLACK;
    }


    @Override
    public LotteKeyframeAnimation animationForKeyPath(@AnimatableProperty int property) {
        if (!hasAnimation()) {
            return null;
        }
        LotteKeyframeAnimation animation = new LotteColorKeyframeAnimation(property, duration, keyTimes, colorKeyframes);
        animation.setStartDelay(delay);
        animation.setInterpolators(timingFunctions);
        animation.addUpdateListener(new LotteKeyframeAnimation.AnimationListener() {
            @Override
            public void onValueChanged(Object progress) {
                observable.setValue((Integer) progress);
            }
        });
        return animation;
    }

    @Override
    public boolean hasAnimation() {
        return !colorKeyframes.isEmpty();
    }

    @Override
    public Observable<Integer> getObservable() {
        return observable;
    }

    @ColorInt
    public int getInitialColor() {
        return initialColor;
    }

    @Override
    public String toString() {
        return "LotteAnimatableColorValue{" + "initialColor=" + initialColor + '}';
    }
}
