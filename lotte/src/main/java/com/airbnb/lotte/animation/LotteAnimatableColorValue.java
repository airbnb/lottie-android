package com.airbnb.lotte.animation;

import android.graphics.Color;

import com.airbnb.lotte.utils.LotteColorKeyframeAnimation;
import com.airbnb.lotte.utils.LotteKeyframeAnimation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LotteAnimatableColorValue extends BaseLotteAnimatableValue<Integer, Integer> {

    public LotteAnimatableColorValue(JSONObject json, int frameRate, long compDuration) {
        super(json, frameRate, compDuration);
    }

    @Override
    protected Integer valueFromObject(Object object) throws JSONException {
        JSONArray colorArray = (JSONArray) object;
        if (colorArray.length() == 4) {
            boolean shouldUse255 = true;
            for (int i = 0; i < colorArray.length(); i++) {
                double colorChannel = colorArray.getDouble(i);
                if (colorChannel > 1f) {
                    shouldUse255 = false;
                }
            }

            float multiplier = shouldUse255 ? 255f : 1f;
            return Color.argb(
                    (int) (colorArray.getDouble(3) * multiplier),
                    (int) (colorArray.getDouble(0) * multiplier),
                    (int) (colorArray.getDouble(1) * multiplier),
                    (int) (colorArray.getDouble(2) * multiplier));
        }
        return Color.BLACK;
    }


    @Override
    public LotteKeyframeAnimation animationForKeyPath() {
        if (!hasAnimation()) {
            return null;
        }
        LotteColorKeyframeAnimation animation = new LotteColorKeyframeAnimation(duration, compDuration, keyTimes, keyValues, interpolators);
        animation.setStartDelay(delay);
        animation.addUpdateListener(new LotteKeyframeAnimation.AnimationListener<Integer>() {
            @Override
            public void onValueChanged(Integer progress) {
                observable.setValue(progress);
            }
        });
        return animation;
    }

    @Override
    public String toString() {
        return "LotteAnimatableColorValue{" + "initialValue=" + initialValue + '}';
    }
}
