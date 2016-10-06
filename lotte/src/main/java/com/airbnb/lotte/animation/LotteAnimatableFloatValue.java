package com.airbnb.lotte.animation;

import com.airbnb.lotte.utils.LotteKeyframeAnimation;
import com.airbnb.lotte.utils.LotteNumberKeyframeAnimation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LotteAnimatableFloatValue extends BaseLotteAnimatableValue<Float> {
    public LotteAnimatableFloatValue(JSONObject json, int frameRate, long compDuration) {
        super(json, frameRate, compDuration);
    }

    @Override
    protected Float valueFromObject(Object object) throws JSONException {
        if (object instanceof JSONArray) {
            object = ((JSONArray) object).get(0);
        }
        if (object instanceof Float) {
            return (Float) object;
        } else if (object instanceof Double) {
            return new Float((Double) object);
        } else if (object instanceof Integer) {
            return new Float((Integer) object);
        }
        return null;
    }

    @Override
    public LotteKeyframeAnimation animationForKeyPath(@LotteAnimatableProperty.AnimatableProperty int property) {
        LotteKeyframeAnimation<Float> animation = new LotteNumberKeyframeAnimation<>(property, duration, compDuration, keyTimes, Float.class, keyValues);
        animation.setStartDelay(delay);
        animation.addUpdateListener(new LotteKeyframeAnimation.AnimationListener<Float>() {
            @Override
            public void onValueChanged(Float progress) {
                observable.setValue(progress);
            }
        });
        return animation;
    }
}
