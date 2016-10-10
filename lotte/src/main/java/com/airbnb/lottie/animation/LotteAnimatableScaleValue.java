package com.airbnb.lottie.animation;

import com.airbnb.lottie.utils.LotteKeyframeAnimation;
import com.airbnb.lottie.utils.LotteTransform3D;
import com.airbnb.lottie.utils.LotteTransformKeyframeAnimation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"EmptyCatchBlock"})
public class LotteAnimatableScaleValue extends BaseLotteAnimatableValue<LotteTransform3D, LotteTransform3D> {

    public LotteAnimatableScaleValue(JSONObject scaleValues, int frameRate, long compDuration) {
        super(scaleValues, frameRate, compDuration);
    }

    @Override
    protected LotteTransform3D valueFromObject(Object object) throws JSONException {
        JSONArray array = (JSONArray) object;
        try {
            if (array.length() >= 2) {
                return new LotteTransform3D().scale((float) array.getDouble(0) / 100f, (float) array.getDouble(1) / 100f);
            }
        } catch (JSONException e) { }

        return new LotteTransform3D();
    }

    @Override
    public LotteKeyframeAnimation animationForKeyPath() {
        LotteTransformKeyframeAnimation animation = new LotteTransformKeyframeAnimation(duration, compDuration, keyTimes, keyValues, interpolators);
        animation.setStartDelay(delay);
        animation.addUpdateListener(new LotteKeyframeAnimation.AnimationListener<LotteTransform3D>() {
            @Override
            public void onValueChanged(LotteTransform3D progress) {
                observable.setValue(progress);
            }
        });
        return animation;
    }
}
