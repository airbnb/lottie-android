package com.airbnb.lotte.animation;

import android.graphics.PointF;

import com.airbnb.lotte.utils.LotteKeyframeAnimation;
import com.airbnb.lotte.utils.LottePointKeyframeAnimation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LotteAnimatablePointValue extends BaseLotteAnimatableValue<PointF> {
    public LotteAnimatablePointValue(JSONObject pointValues, int frameRate, long compDuration) {
        super(pointValues, frameRate, compDuration);
    }

    @Override
    protected PointF valueFromObject(Object object) throws JSONException {
        if (object instanceof JSONArray) {
            return pointFromJsonArray((JSONArray) object);
        } else if (object instanceof JSONObject) {
            return pointFromJsonObject((JSONObject) object);
        }
        throw new IllegalArgumentException("Unable to parse point from " + object);
    }

    private PointF pointFromJsonArray(JSONArray values) {
        if (values.length() >= 2) {
            try {
                return new PointF((float) values.getDouble(0), (float) values.getDouble(1));
            } catch (JSONException e) {
                throw new IllegalArgumentException("Unable to parse point for " + values, e);
            }
        }

        return new PointF();
    }

    private PointF pointFromJsonObject(JSONObject value) {
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

            return point;
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to parse point for " + value);
        }
    }

    @Override
    public LotteKeyframeAnimation animationForKeyPath() {
        if (!hasAnimation()) {
            return null;
        }

        LotteKeyframeAnimation<PointF> animation = new LottePointKeyframeAnimation(duration, compDuration, keyTimes, keyValues, interpolators);
        animation.setStartDelay(delay);
        animation.addUpdateListener(new LotteKeyframeAnimation.AnimationListener<PointF>() {
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
