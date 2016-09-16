package com.airbnb.lotte.animation;

import android.support.annotation.FloatRange;

import com.airbnb.lotte.utils.LotteKeyframeAnimation;
import com.airbnb.lotte.utils.LotteTransform3D;

import java.util.List;

public class LotteTransformKeyframeAnimation extends LotteKeyframeAnimation<LotteTransform3D> {
    private final List<LotteTransform3D> transforms;

    public LotteTransformKeyframeAnimation(String objectProperty, long duration, List<Float> keyTimes, List<LotteTransform3D> transforms) {
        super(objectProperty, duration, keyTimes);
        this.transforms = transforms;
    }

    @Override
    public LotteTransform3D getValueForProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        // TODO
        return null;
    }
}
