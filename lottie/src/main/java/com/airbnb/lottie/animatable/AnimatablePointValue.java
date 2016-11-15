package com.airbnb.lottie.animatable;

import android.graphics.PointF;

import com.airbnb.lottie.utils.JsonUtils;
import com.airbnb.lottie.animation.KeyframeAnimation;
import com.airbnb.lottie.animation.PointKeyframeAnimation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AnimatablePointValue extends BaseAnimatableValue<PointF, PointF> {

    public AnimatablePointValue(JSONObject pointValues, int frameRate, long compDuration) {
        super(pointValues, frameRate, compDuration, true);
    }

    @Override
    protected PointF valueFromObject(Object object, float scale) throws JSONException {
        if (object instanceof JSONArray) {
            return JsonUtils.pointFromJsonArray((JSONArray) object, scale);
        } else if (object instanceof JSONObject) {
            return JsonUtils.pointValueFromJsonObject((JSONObject) object, scale);
        }
        throw new IllegalArgumentException("Unable to parse point from " + object);
    }

    @Override
    public KeyframeAnimation<PointF> animationForKeyPath() {
        if (!hasAnimation()) {
            return null;
        }

        KeyframeAnimation<PointF> animation = new PointKeyframeAnimation(duration, compDuration, keyTimes, keyValues, interpolators);
        animation.setStartDelay(delay);
        animation.addUpdateListener(new KeyframeAnimation.AnimationListener<PointF>() {
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
