package com.airbnb.lottie.animatable;

import android.support.annotation.RestrictTo;

import com.airbnb.lottie.animation.KeyframeAnimation;
import com.airbnb.lottie.animation.NumberKeyframeAnimation;
import com.airbnb.lottie.animation.StaticKeyframeAnimation;
import com.airbnb.lottie.model.LottieComposition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@RestrictTo(RestrictTo.Scope.GROUP_ID)
public class AnimatableIntegerValue extends BaseAnimatableValue<Integer, Integer> {


    public AnimatableIntegerValue(JSONObject json, int frameRate, LottieComposition composition, boolean isDp, boolean remap100To255) {
        super(json, frameRate, composition, isDp);
        if (remap100To255) {
            initialValue = initialValue * 255 / 100;
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
    public KeyframeAnimation<Integer> createAnimation() {
        if (!hasAnimation()) {
            return new StaticKeyframeAnimation<>(initialValue);
        }

        KeyframeAnimation<Integer> animation = new NumberKeyframeAnimation<>(duration, composition, keyTimes, Integer.class, keyValues, interpolators);
        animation.setStartDelay(delay);
        return animation;
    }

    public Integer getInitialValue() {
        return initialValue;
    }
}
