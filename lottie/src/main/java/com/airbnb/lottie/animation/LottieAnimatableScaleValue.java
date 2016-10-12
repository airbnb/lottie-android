package com.airbnb.lottie.animation;

import com.airbnb.lottie.utils.LottieKeyframeAnimation;
import com.airbnb.lottie.utils.LottieTransform3D;
import com.airbnb.lottie.utils.LottieTransformKeyframeAnimation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"EmptyCatchBlock"})
public class LottieAnimatableScaleValue extends BaseLottieAnimatableValue<LottieTransform3D, LottieTransform3D> {

    public LottieAnimatableScaleValue(JSONObject scaleValues, int frameRate, long compDuration) {
        this(scaleValues, frameRate, compDuration, true);
    }

    public LottieAnimatableScaleValue(JSONObject scaleValues, int frameRate, long compDuration, boolean isDp) {
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
    public LottieKeyframeAnimation animationForKeyPath() {
        LottieTransformKeyframeAnimation animation = new LottieTransformKeyframeAnimation(duration, compDuration, keyTimes, keyValues, interpolators);
        animation.setStartDelay(delay);
        animation.addUpdateListener(new LottieKeyframeAnimation.AnimationListener<LottieTransform3D>() {
            @Override
            public void onValueChanged(LottieTransform3D progress) {
                observable.setValue(progress);
            }
        });
        return animation;
    }
}
