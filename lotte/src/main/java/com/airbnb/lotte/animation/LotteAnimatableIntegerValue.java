package com.airbnb.lotte.animation;

import com.airbnb.lotte.utils.LotteKeyframeAnimation;
import com.airbnb.lotte.utils.LotteNumberKeyframeAnimation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LotteAnimatableIntegerValue extends BaseLotteAnimatableValue<Integer> {
    public LotteAnimatableIntegerValue(JSONObject json, int frameRate, long compDuration) {
        super(json, frameRate, compDuration);
    }

    @Override
    protected Integer valueFromObject(Object object) throws JSONException {
        if (object instanceof Integer) {
            return (Integer) object;
        } else if (object instanceof JSONArray && ((JSONArray) object).get(0) instanceof Integer) {
            return ((JSONArray) object).getInt(0);
        }
        return null;
    }

    @Override
    public LotteKeyframeAnimation animationForKeyPath() {
        LotteKeyframeAnimation<Integer> animation = new LotteNumberKeyframeAnimation<>(duration, compDuration, keyTimes, Integer.class, keyValues);
        animation.setStartDelay(delay);
        animation.addUpdateListener(new LotteKeyframeAnimation.AnimationListener<Integer>() {
            @Override
            public void onValueChanged(Integer progress) {
                observable.setValue(progress);
            }
        });
        return animation;
    }
}
