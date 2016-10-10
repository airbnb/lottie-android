package com.airbnb.lottie.animation;

import android.support.annotation.Nullable;

import com.airbnb.lottie.model.RemapInterface;
import com.airbnb.lottie.utils.LottieKeyframeAnimation;
import com.airbnb.lottie.utils.LottieNumberKeyframeAnimation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LottieAnimatableFloatValue extends BaseLottieAnimatableValue<Float, Float> {

    @Nullable private RemapInterface<Float> remapInterface;

    public LottieAnimatableFloatValue(JSONObject json, int frameRate, long compDuration) {
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

    public void remapValues(final float fromMin, final float fromMax, final float toMin, final float toMax) {
        remapInterface = new RemapInterface<Float>() {
            @Override
            public Float remap(Float inValue) {
                if (inValue < fromMin) {
                    return toMin;
                } else if (inValue > fromMax) {
                    return toMax;
                } else {
                    return toMin + (inValue / (fromMax - fromMin) * (toMax - toMin));
                }
            }
        };
        observable.setValue(remapInterface.remap(observable.getValue()));
    }

    public Float getInitialValue() {
        if (remapInterface != null) {
            return remapInterface.remap(initialValue);
        }
        return initialValue;
    }
}
