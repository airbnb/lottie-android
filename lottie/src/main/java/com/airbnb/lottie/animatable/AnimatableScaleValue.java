package com.airbnb.lottie.animatable;

import com.airbnb.lottie.animation.KeyframeAnimation;
import com.airbnb.lottie.utils.ScaleXY;
import com.airbnb.lottie.animation.ScaleKeyframeAnimation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"EmptyCatchBlock"})
public class AnimatableScaleValue extends BaseAnimatableValue<ScaleXY, ScaleXY> {

    public AnimatableScaleValue(JSONObject scaleValues, int frameRate, long compDuration, boolean isDp) {
        super(scaleValues, frameRate, compDuration, isDp);
    }

    @Override
    protected ScaleXY valueFromObject(Object object, float scale) throws JSONException {
        JSONArray array = (JSONArray) object;
        try {
            if (array.length() >= 2) {
                return new ScaleXY().scale((float) array.getDouble(0) / 100f * scale, (float) array.getDouble(1) / 100f * scale);
            }
        } catch (JSONException e) { }

        return new ScaleXY();
    }

    @Override
    public KeyframeAnimation animationForKeyPath() {
        ScaleKeyframeAnimation animation = new ScaleKeyframeAnimation(duration, compDuration, keyTimes, keyValues, interpolators);
        animation.setStartDelay(delay);
        animation.addUpdateListener(new KeyframeAnimation.AnimationListener<ScaleXY>() {
            @Override
            public void onValueChanged(ScaleXY progress) {
                observable.setValue(progress);
            }
        });
        return animation;
    }
}
