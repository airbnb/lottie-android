package com.airbnb.lottie.animatable;

import android.graphics.Color;

import com.airbnb.lottie.animation.ColorKeyframeAnimation;
import com.airbnb.lottie.animation.KeyframeAnimation;
import com.airbnb.lottie.model.LottieComposition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AnimatableColorValue extends BaseAnimatableValue<Integer, Integer> {

    public AnimatableColorValue(JSONObject json, int frameRate, LottieComposition composition) {
        super(json, frameRate, composition, false);
    }

    @Override
    protected Integer valueFromObject(Object object, float scale) throws JSONException {
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
    public KeyframeAnimation animationForKeyPath() {
        if (!hasAnimation()) {
            return null;
        }
        ColorKeyframeAnimation animation = new ColorKeyframeAnimation(duration, composition, keyTimes, keyValues, interpolators);
        animation.setStartDelay(delay);
        animation.addUpdateListener(new KeyframeAnimation.AnimationListener<Integer>() {
            @Override
            public void onValueChanged(Integer progress) {
                observable.setValue(progress);
            }
        });
        return animation;
    }

    @Override
    public String toString() {
        return "AnimatableColorValue{" + "initialValue=" + initialValue + '}';
    }
}
