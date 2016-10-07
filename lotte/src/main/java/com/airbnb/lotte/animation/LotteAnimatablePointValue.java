package com.airbnb.lotte.animation;

import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.airbnb.lotte.utils.LotteKeyframeAnimation;
import com.airbnb.lotte.utils.LottePathKeyframeAnimation;
import com.airbnb.lotte.utils.LottePointKeyframeAnimation;
import com.airbnb.lotte.utils.Observable;
import com.airbnb.lotte.utils.SegmentedPath;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LotteAnimatablePointValue implements LotteAnimatableValue<PointF> {
    private static final String TAG = LotteAnimatablePointValue.class.getSimpleName();

    private final Observable<PointF> observable = new Observable<>();
    private final List<PointF> pointKeyframes = new ArrayList<>();
    private final List<Float> keyTimes = new ArrayList<>();
    private final List<Interpolator> interpolators = new ArrayList<>();
    private final long compDuration;

    private boolean usePathAnimation = true;
    private PointF initialPoint;
    private final SegmentedPath animationPath = new SegmentedPath();
    private long delay;
    private long duration;
    private long startFrame;
    private long durationFrames;
    private int frameRate;

    public LotteAnimatablePointValue(JSONObject pointValues, int frameRate, long compDuration) {
        this.compDuration = compDuration;
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
                observable.setValue(initialPoint);
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
                    pointKeyframes.add(vertex);
                    interpolators.add(new LinearInterpolator());
                    outPoint = null;
                }

                PointF startPoint = keyframe.has("s") ? pointFromValueArray(keyframe.getJSONArray("s")) : new PointF();
                if (addStartValue) {
                    if (i == 0) {
                        pointKeyframes.add(startPoint);
                        animationPath.moveTo(startPoint.x, startPoint.y);
                        initialPoint = startPoint;
                        observable.setValue(initialPoint);
                    } else {
                        animationPath.lineTo(startPoint.x, startPoint.y);
                        pointKeyframes.add(startPoint);
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
                    cp1 = keyframe.has("to") ? pointFromValueArray(keyframe.getJSONArray("to")) : null;
                    cp2 = keyframe.has("ti") ? pointFromValueArray(keyframe.getJSONArray("ti")) : null;
                    PointF vertex = pointFromValueArray(keyframe.getJSONArray("e"));
                    pointKeyframes.add(vertex);
                    if (cp1 != null && cp2 != null) {
                        PointF inVertex = startPoint;
                        animationPath.cubicTo(
                                inVertex.x + cp1.x, inVertex.y + cp1.y,
                                vertex.x + cp2.x, vertex.y + cp2.y,
                                vertex.x, vertex.y);
                    } else {
                        animationPath.lineTo(vertex.x, vertex.y);
                    }

                    Interpolator interpolator;
                    if (keyframe.has("o") && keyframe.has("i")) {
                        cp1 = pointFromValueObject(keyframe.getJSONObject("o"));
                        cp2 = pointFromValueObject(keyframe.getJSONObject("i"));
                        interpolator = PathInterpolatorCompat.create(cp1.x, cp1.y, cp2.x, cp2.y);
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

            observable.setValue(pointKeyframes.get(0));
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse keyframes " + keyframes, e);
        }
    }

    private PointF pointFromValueArray(JSONArray values) {
        if (values.length() >= 2) {
            try {
                return new PointF((float) values.getDouble(0), (float) values.getDouble(1));
            } catch (JSONException e) {
                throw new IllegalArgumentException("Unable to parse point for " + values, e);
            }
        }

        return new PointF();
    }

    private PointF pointFromValueObject(JSONObject value) {
        try {
            Object x = value.get("x");
            Object y = value.get("y");

            PointF point = new PointF();
            if (x instanceof JSONArray) {
                point.x = new Float(((JSONArray) x).getDouble(0));
            } else {
                if (x instanceof Integer) {
                    point.x = (Integer) x;
                } else {
                    point.x = new Float((Double) x);
                }
            }

            if (y instanceof JSONArray) {
                point.y = new Float(((JSONArray) y).getDouble(0));
            } else {
                if (y instanceof Integer) {
                    point.y = (Integer) y;
                } else {
                    point.y = new Float((Double) y);
                }
            }

            return point;
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse point for " + value);
        }
    }


    public void remapPointsFromBounds(Rect fromBounds, Rect toBounds) {
        // TODO: this is broken. Maybe not necessary.
//        if (pointKeyframes.isEmpty()) {
//            initialPoint = new PointF(
//                    MiscUtils.remapValue(initialPoint.x, fromBounds.left, fromBounds.width(), toBounds.left, toBounds.width()),
//                    MiscUtils.remapValue(initialPoint.y, fromBounds.top, fromBounds.height(), toBounds.top, toBounds.height()));
//        } else {
//            for (PointF point : pointKeyframes) {
//               point.set(
//                        MiscUtils.remapValue(point.x, fromBounds.left, fromBounds.width(), toBounds.left, toBounds.width()),
//                        MiscUtils.remapValue(point.y, fromBounds.top, fromBounds.height(), toBounds.top, toBounds.height()));
//
//            }
//        }
    }

    public void setUsePathAnimation(boolean usePathAnimation) {
        this.usePathAnimation = usePathAnimation;
    }

    @Override
    public Observable<PointF> getObservable() {
        return observable;
    }

    public PointF getInitialPoint() {
        return initialPoint;
    }

    @Override
    public LotteKeyframeAnimation animationForKeyPath() {
        if (!hasAnimation()) {
            return null;
        }

        LotteKeyframeAnimation animation;
        if (!animationPath.isEmpty() && usePathAnimation) {
            animation = new LottePathKeyframeAnimation(duration, compDuration, keyTimes, animationPath);
        } else {
            animation = new LottePointKeyframeAnimation(duration, compDuration, keyTimes, pointKeyframes);
        }
        animation.addUpdateListener(new LotteKeyframeAnimation.AnimationListener<PointF>() {
            @Override
            public void onValueChanged(PointF progress) {
                observable.setValue(progress);
            }
        });
        animation.setStartDelay(delay);
        return animation;
    }

    @Override
    public boolean hasAnimation() {
        return !animationPath.isEmpty() || !pointKeyframes.isEmpty();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LotteAnimatablePointValue{");
        sb.append("initialPoint=").append(initialPoint);
        sb.append('}');
        return sb.toString();
    }
}
