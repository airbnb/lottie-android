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
public class AnimatableFloatValue extends BaseAnimatableValue<Float, Float> {

    public AnimatableFloatValue(JSONObject json, int frameRate, LottieComposition composition) {
        this(json, frameRate, composition, true);
    }

    public AnimatableFloatValue(JSONObject json, int frameRate, LottieComposition composition, boolean isDp) {
        super(json, frameRate, composition, isDp);
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
    public KeyframeAnimation<Float> createAnimation() {
        if (!hasAnimation()) {
            return new StaticKeyframeAnimation<>(initialValue);
        }

        KeyframeAnimation<Float> animation = new NumberKeyframeAnimation<>(duration, composition, keyTimes, Float.class, keyValues, interpolators);
        animation.setStartDelay(delay);
        return animation;
    }

    public Float getInitialValue() {
        return initialValue;
    }
}
