package com.airbnb.lottie.animation;

import android.graphics.PointF;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.airbnb.lottie.L;
import com.airbnb.lottie.utils.JsonUtils;
import com.airbnb.lottie.utils.LottieKeyframeAnimation;
import com.airbnb.lottie.utils.LottiePathKeyframeAnimation;
import com.airbnb.lottie.utils.Observable;
import com.airbnb.lottie.utils.SegmentedPath;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LottieAnimatablePathValue implements LottieAnimatableValue<PointF> {

    private final Observable<PointF> observable = new Observable<>();
    private final List<Float> keyTimes = new ArrayList<>();
    private final List<Interpolator> interpolators = new ArrayList<>();
    private final long compDuration;
    private final int frameRate;

    private PointF initialPoint;
    private final SegmentedPath animationPath = new SegmentedPath();
    private long delay;
    private long duration;
    private long startFrame;
    private long durationFrames;

    public LottieAnimatablePathValue(JSONObject pointValues, int frameRate, long compDuration) {
        this.compDuration = compDuration;
        this.frameRate = frameRate;

        Object value;
        try {
            value = pointValues.get("k");
        } catch (JSONException e) {
            throw new IllegalArgumentException("Point values have no keyframes.");
        }

        if (value instanceof JSONArray) {
            Object firstObject;
            try {
                firstObject = ((JSONArray) value).get(0);
            } catch (JSONException e) {
                throw new IllegalArgumentException("Unable to parse value.");
            }

            if (firstObject instanceof JSONObject && ((JSONObject) firstObject).has("t")) {
                // Keyframes
                buildAnimationForKeyframes((JSONArray) value);
            } else {
                // Single Value, no animation
                initialPoint = JsonUtils.pointFromJsonArray((JSONArray) value, L.SCALE);
                observable.setValue(initialPoint);
            }
        }

    }

    @SuppressWarnings("Duplicates")
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
            PointF outPoint = null;

            for (int i = 0; i < keyframes.length(); i++) {
                JSONObject keyframe = keyframes.getJSONObject(i);
                long frame = keyframe.getLong("t");
                float timePercentage = (float) (frame - startFrame) / (float) durationFrames;

                if (outPoint != null) {
                    PointF vertex = outPoint;
                    animationPath.lineTo(vertex.x, vertex.y);
                    interpolators.add(new LinearInterpolator());
                    outPoint = null;
                }

                PointF startPoint = keyframe.has("s") ? JsonUtils.pointFromJsonArray(keyframe.getJSONArray("s"), L.SCALE) : new PointF();
                if (addStartValue) {
                    if (i == 0) {
                        animationPath.moveTo(startPoint.x, startPoint.y);
                        initialPoint = startPoint;
                        observable.setValue(initialPoint);
                    } else {
                        animationPath.lineTo(startPoint.x, startPoint.y);
                        interpolators.add(new LinearInterpolator());
                    }
                    addStartValue = false;
                }

                if (addTimePadding) {
                    float holdPercentage = timePercentage - 0.00001f;
                    keyTimes.add(holdPercentage);
                    addTimePadding = false;
                }

                PointF cp1;
                PointF cp2;
                if (keyframe.has("e")) {
                    cp1 = keyframe.has("to") ? JsonUtils.pointFromJsonArray(keyframe.getJSONArray("to"), L.SCALE) : null;
                    cp2 = keyframe.has("ti") ? JsonUtils.pointFromJsonArray(keyframe.getJSONArray("ti"), L.SCALE) : null;
                    PointF vertex = JsonUtils.pointFromJsonArray(keyframe.getJSONArray("e"), L.SCALE);
                    if (cp1 != null && cp2 != null) {
                        animationPath.cubicTo(
                                startPoint.x + cp1.x, startPoint.y + cp1.y,
                                vertex.x + cp2.x, vertex.y + cp2.y,
                                vertex.x, vertex.y);
                    } else {
                        animationPath.lineTo(vertex.x, vertex.y);
                    }

                    Interpolator interpolator;
                    if (keyframe.has("o") && keyframe.has("i")) {
                        cp1 = JsonUtils.pointValueFromJsonObject(keyframe.getJSONObject("o"), L.SCALE);
                        cp2 = JsonUtils.pointValueFromJsonObject(keyframe.getJSONObject("i"), L.SCALE);
                        interpolator = PathInterpolatorCompat.create(cp1.x / L.SCALE, cp1.y / L.SCALE, cp2.x / L.SCALE, cp2.y / L.SCALE);
                    } else {
                        interpolator = new LinearInterpolator();
                    }
                    interpolators.add(interpolator);
                }

                keyTimes.add(timePercentage);

                if (keyframe.has("h") && keyframe.getInt("h") == 1) {
                    outPoint = startPoint;
                    addStartValue = true;
                    addTimePadding = true;
                }
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse keyframes " + keyframes, e);
        }
    }

    @Override
    public Observable<PointF> getObservable() {
        return observable;
    }

    @Override
    public LottieKeyframeAnimation animationForKeyPath() {
        if (!hasAnimation()) {
            return null;
        }

        LottieKeyframeAnimation<PointF> animation = new LottiePathKeyframeAnimation(duration, compDuration, keyTimes, animationPath, interpolators);
        animation.setStartDelay(delay);
        animation.addUpdateListener(new LottieKeyframeAnimation.AnimationListener<PointF>() {
            @Override
            public void onValueChanged(PointF progress) {
                observable.setValue(progress);
            }
        });
        return animation;
    }

    @Override
    public boolean hasAnimation() {
        return animationPath.hasSegments();
    }

    @Override
    public String toString() {
        return "LottieAnimatablePathValue{" + "initialPoint=" + initialPoint + '}';
    }
}
