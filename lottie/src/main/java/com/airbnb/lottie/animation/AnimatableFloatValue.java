package com.airbnb.lottie.animation;

import com.airbnb.lottie.utils.LottieKeyframeAnimation;
import com.airbnb.lottie.utils.LottieNumberKeyframeAnimation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AnimatableFloatValue extends BaseAnimatableValue<Float, Float> {

    public AnimatableFloatValue(JSONObject json, int frameRate, long compDuration) {
        this(json, frameRate, compDuration, true);
    }

    public AnimatableFloatValue(JSONObject json, int frameRate, long compDuration, boolean isDp) {
        super(json, frameRate, compDuration, isDp);
    }

    @Override
    protected Float valueFromObject(Object object, float scale) throws JSONException {
        if (object instanceof JSONArray) {
            object = ((JSONArray) object).get(0);
        }
        if (object instanceof Float) {
            return (Float) object * scale;
        } else if (object instanceof Double) {
            return (float) ((Double) object * scale);
        } else if (object instanceof Integer) {
            return (Integer) object * scale;
        }
        return null;
    }

    @Override
    public LottieKeyframeAnimation animationForKeyPath() {
        LottieKeyframeAnimation<Float> animation = new LottieNumberKeyframeAnimation<>(duration, compDuration, keyTimes, Float.class, keyValues, interpolators);
        animation.setStartDelay(delay);
        animation.addUpdateListener(new LottieKeyframeAnimation.AnimationListener<Float>() {
            @Override
            public void onValueChanged(Float progress) {
                observable.setValue(progress);
            }
        });
        return animation;
    }

    public Float getInitialValue() {
        return initialValue;
    }
}
