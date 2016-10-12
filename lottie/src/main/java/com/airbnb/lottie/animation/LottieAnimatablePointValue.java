package com.airbnb.lottie.animation;

import android.graphics.PointF;

import com.airbnb.lottie.utils.LottieKeyframeAnimation;
import com.airbnb.lottie.utils.LottiePointKeyframeAnimation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LottieAnimatablePointValue extends BaseLottieAnimatableValue<PointF, PointF> {

    public LottieAnimatablePointValue(JSONObject pointValues, int frameRate, long compDuration) {
        this(pointValues, frameRate, compDuration, true);
    }

    private LottieAnimatablePointValue(JSONObject pointValues, int frameRate, long compDuration, boolean isDp) {
        super(pointValues, frameRate, compDuration, isDp);
    }

    @Override
    protected PointF valueFromObject(Object object, float scale) throws JSONException {
        if (object instanceof JSONArray) {
            return pointFromJsonArray((JSONArray) object, scale);
        } else if (object instanceof JSONObject) {
            return pointFromJsonObject((JSONObject) object, scale);
        }
        throw new IllegalArgumentException("Unable to parse point from " + object);
    }

    private PointF pointFromJsonArray(JSONArray values, float scale) {
        if (values.length() >= 2) {
            try {
                return new PointF((float) values.getDouble(0) * scale, (float) values.getDouble(1) * scale);
            } catch (JSONException e) {
                throw new IllegalArgumentException("Unable to parse point for " + values, e);
            }
        }

        return new PointF();
    }

    private PointF pointFromJsonObject(JSONObject value, float scale) {
        try {
            Object x = value.get("x");
            Object y = value.get("y");

            PointF point = new PointF();
            if (x instanceof JSONArray) {
                point.x = (float) ((JSONArray) x).getDouble(0);
            } else {
                if (x instanceof Integer) {
                    point.x = (Integer) x;
                } else {
                    point.x = new Float((Double) x);
                }
            }

            if (y instanceof JSONArray) {
                point.y = (float) ((JSONArray) y).getDouble(0);
            } else {
                if (y instanceof Integer) {
                    point.y = (Integer) y;
                } else {
                    point.y = new Float((Double) y);
                }
            }

            point.x *= scale;
            point.y *= scale;
            return point;
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse point for " + value);
        }
    }

    @Override
    public LottieKeyframeAnimation animationForKeyPath() {
        if (!hasAnimation()) {
            return null;
        }

        LottieKeyframeAnimation<PointF> animation = new LottiePointKeyframeAnimation(duration, compDuration, keyTimes, keyValues, interpolators);
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
        return !keyValues.isEmpty();
    }
}
