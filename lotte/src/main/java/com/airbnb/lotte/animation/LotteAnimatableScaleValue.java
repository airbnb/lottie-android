package com.airbnb.lotte.animation;

import android.graphics.PointF;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.airbnb.lotte.utils.JsonUtils;
import com.airbnb.lotte.utils.LotteKeyframeAnimation;
import com.airbnb.lotte.utils.LotteTransform3D;
import com.airbnb.lotte.utils.LotteTransformKeyframeAnimation;
import com.airbnb.lotte.utils.Observable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"EmptyCatchBlock"})
public class LotteAnimatableScaleValue implements LotteAnimatableValue<LotteTransform3D> {

    private final Observable<LotteTransform3D> observable = new Observable<>();
    private LotteTransform3D initialScale;
    private final List<LotteTransform3D> scaleKeyframes = new ArrayList<>();
    private final List<Float> keyTimes = new ArrayList<>();
    private final List<Interpolator> interpolators = new ArrayList<>();
    private final int frameRate;
    private final long compDuration;

    private long delay;
    private long duration;


    public LotteAnimatableScaleValue(JSONObject scaleValues, int frameRate, long compDuration) {
        this.frameRate = frameRate;
        this.compDuration = compDuration;
        try {
            Object value = scaleValues.get("k");
            if (value instanceof JSONArray) {
                Object firstChild = ((JSONArray) value).get(0);
                if (firstChild instanceof JSONObject && ((JSONObject) firstChild).has("t")) {
                    // Keyframes
                    buildAnimationForKeyframes((JSONArray) value);
                } else {
                    // Single value, no animation.
                    initialScale = transformForValueArray((JSONArray) value);
                    observable.setValue(initialScale);
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
            int startFrame = keyframes.getJSONObject(0).getInt("t");
            int endFrame = keyframes.getJSONObject(keyframes.length() - 1).getInt("t");

            if (endFrame <= startFrame) {
                throw new IllegalArgumentException("End frame must be after start frame " + endFrame + " vs " + startFrame);
            }

            int durationFrames = endFrame - startFrame;

            duration = (long) (durationFrames / (float) frameRate * 1000);
            delay = (long) (startFrame / (float) frameRate * 1000);

            boolean addStartValue = true;
            boolean addTimePadding = false;
            LotteTransform3D outValue = null;

            for (int i = 0; i < keyframes.length(); i++) {
                JSONObject keyframe = keyframes.getJSONObject(i);
                int frame = keyframe.getInt("t");
                float timePercentage = (float) (frame - startFrame) / (float) durationFrames;

                if (outValue != null) {
                    scaleKeyframes.add(outValue);
                    interpolators.add(new LinearInterpolator());
                    outValue = null;
                }

                LotteTransform3D startValue = null;
                if (addStartValue) {
                    if (keyframe.has("s")) {
                        startValue = transformForValueArray(keyframe.getJSONArray("s"));
                        if (i == 0) {
                            initialScale = startValue;
                            observable.setValue(initialScale);
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
                    LotteTransform3D endValue = transformForValueArray(keyframe.getJSONArray("e"));
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

    private LotteTransform3D transformForValueArray(JSONArray value) {
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
    public LotteKeyframeAnimation animationForKeyPath() {
        LotteTransformKeyframeAnimation animation = new LotteTransformKeyframeAnimation(duration, compDuration, keyTimes, scaleKeyframes, interpolators);
        animation.setStartDelay(delay);
        animation.addUpdateListener(new LotteKeyframeAnimation.AnimationListener<LotteTransform3D>() {
            @Override
            public void onValueChanged(LotteTransform3D progress) {
                observable.setValue(progress);
            }
        });
        return animation;
    }

    @Override
    public boolean hasAnimation() {
        return !scaleKeyframes.isEmpty();
    }

    @Override
    public Observable<LotteTransform3D> getObservable() {
        return observable;
    }

    @Override
    public String toString() {
        return "LotteAnimatableScaleValue{" + "initialScale=" + initialScale + '}';
    }
}
