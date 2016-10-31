package com.airbnb.lottie.animatable;

import com.airbnb.lottie.animation.KeyframeAnimation;
import com.airbnb.lottie.utils.LottieTransform3D;
import com.airbnb.lottie.animation.TransformKeyframeAnimation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"EmptyCatchBlock"})
public class AnimatableScaleValue extends BaseAnimatableValue<LottieTransform3D, LottieTransform3D> {

    public AnimatableScaleValue(JSONObject scaleValues, int frameRate, long compDuration, boolean isDp) {
        super(scaleValues, frameRate, compDuration, isDp);
    }

    @Override
    protected LottieTransform3D valueFromObject(Object object, float scale) throws JSONException {
        JSONArray array = (JSONArray) object;
        try {
            if (array.length() >= 2) {
                return new LottieTransform3D().scale((float) array.getDouble(0) / 100f * scale, (float) array.getDouble(1) / 100f * scale);
            }
        } catch (JSONException e) { }

        return new LottieTransform3D();
    }

    @Override
    public KeyframeAnimation animationForKeyPath() {
        TransformKeyframeAnimation animation = new TransformKeyframeAnimation(duration, compDuration, keyTimes, keyValues, interpolators);
        animation.setStartDelay(delay);
        animation.addUpdateListener(new KeyframeAnimation.AnimationListener<LottieTransform3D>() {
            @Override
            public void onValueChanged(LottieTransform3D progress) {
                observable.setValue(progress);
            }
        });
        return animation;
    }
}
