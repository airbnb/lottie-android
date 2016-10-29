package com.airbnb.lottie.animation;

import android.support.annotation.Nullable;

import com.airbnb.lottie.model.RemapInterface;
import com.airbnb.lottie.utils.LottieKeyframeAnimation;
import com.airbnb.lottie.utils.LottieNumberKeyframeAnimation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AnimatableIntegerValue extends BaseAnimatableValue<Integer, Integer> {

    @Nullable private RemapInterface<Integer> remapInterface;

    public AnimatableIntegerValue(JSONObject json, int frameRate, long compDuration, boolean isDp) {
        super(json, frameRate, compDuration, isDp);
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
    public LottieKeyframeAnimation animationForKeyPath() {
        LottieKeyframeAnimation<Integer> animation = new LottieNumberKeyframeAnimation<>(duration, compDuration, keyTimes, Integer.class, keyValues, interpolators);
        animation.setStartDelay(delay);
        animation.addUpdateListener(new LottieKeyframeAnimation.AnimationListener<Integer>() {
            @Override
            public void onValueChanged(Integer progress) {
                observable.setValue(progress);
            }
        });
        return animation;
    }

    public void remap100To255() {
        remapValues(0, 100, 0, 255);
    }

    @SuppressWarnings("SameParameterValue")
    private void remapValues(final int fromMin, final int fromMax, final int toMin, final int toMax) {
        remapInterface = new RemapInterface<Integer>() {
            @Override
            public Integer remap(Integer inValue) {
                Integer remappedValue;
                if (inValue < fromMin) {
                    remappedValue = toMin;
                } else if (inValue > fromMax) {
                    remappedValue = toMax;
                } else {
                    remappedValue = (int) (toMin + (inValue / (float) (fromMax - fromMin) * (toMax - toMin)));
                }
                return remappedValue;
            }
        };
        observable.setValue(remapInterface.remap(observable.getValue()));
    }

    public Integer getInitialValue() {
        if (remapInterface != null) {
            return remapInterface.remap(initialValue);
        }
        return initialValue;
    }
}
