package com.airbnb.lottie.animatable;

import com.airbnb.lottie.animation.KeyframeAnimation;
import com.airbnb.lottie.animation.NumberKeyframeAnimation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AnimatableIntegerValue extends BaseAnimatableValue<Integer, Integer> {


    public AnimatableIntegerValue(JSONObject json, int frameRate, long compDuration, boolean isDp, boolean remap100To255) {
        super(json, frameRate, compDuration, isDp);
        if (remap100To255) {
            initialValue = initialValue * 255 / 100;
            getObservable().setValue(initialValue);
            for (int i = 0; i < keyValues.size(); i++) {
                keyValues.set(i, keyValues.get(i) * 255 / 100);
            }
        }
    }

    @Override
    protected Integer valueFromObject(Object object, float scale) throws JSONException {
        if (object instanceof Integer) {
            return Math.round((Integer) object * scale);
        } else if (object instanceof JSONArray && ((JSONArray) object).get(0) instanceof Integer) {
            return Math.round(((JSONArray) object).getInt(0) * scale);
        }
        return null;
    }

    @Override
    public KeyframeAnimation animationForKeyPath() {
        KeyframeAnimation<Integer> animation = new NumberKeyframeAnimation<>(duration, compDuration, keyTimes, Integer.class, keyValues, interpolators);
        animation.setStartDelay(delay);
        animation.addUpdateListener(new KeyframeAnimation.AnimationListener<Integer>() {
            @Override
            public void onValueChanged(Integer progress) {
                observable.setValue(progress);
            }
        });
        return animation;
    }

    public Integer getInitialValue() {
        return initialValue;
    }
}
