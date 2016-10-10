package com.airbnb.lotte.animation;

import android.support.annotation.Nullable;

import com.airbnb.lotte.model.RemapInterface;
import com.airbnb.lotte.utils.LotteKeyframeAnimation;
import com.airbnb.lotte.utils.LotteNumberKeyframeAnimation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LotteAnimatableIntegerValue extends BaseLotteAnimatableValue<Integer, Integer> {

    @Nullable private RemapInterface<Integer> remapInterface;

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
        LotteKeyframeAnimation<Integer> animation = new LotteNumberKeyframeAnimation<>(duration, compDuration, keyTimes, Integer.class, keyValues, interpolators);
        animation.setStartDelay(delay);
        animation.addUpdateListener(new LotteKeyframeAnimation.AnimationListener<Integer>() {
            @Override
            public void onValueChanged(Integer progress) {
                observable.setValue(progress);
            }
        });
        return animation;
    }

    public void remapValues(final int fromMin, final int fromMax, final int toMin, final int toMax) {
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
