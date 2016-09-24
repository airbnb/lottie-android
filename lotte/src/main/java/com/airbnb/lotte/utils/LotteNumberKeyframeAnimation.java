package com.airbnb.lotte.utils;

import android.support.annotation.FloatRange;

import com.airbnb.lotte.animation.LotteAnimatableProperty.AnimatableProperty;

import java.util.List;

import static com.airbnb.lotte.utils.MiscUtils.lerp;

public class LotteNumberKeyframeAnimation<T extends Number> extends LotteKeyframeAnimation<T> {

    private final List<T> values;
    private final Class<T> klass;

    public LotteNumberKeyframeAnimation(@AnimatableProperty int property, long duration, List<Float> keyTimes, Class<T> klass, List<T> values) {
        super(property, duration, keyTimes);
        this.klass = klass;
        if (keyTimes.size() != values.size()) {
            throw new IllegalArgumentException("Key times and values must be the same length " + keyTimes + " vs " + values);
        }
        this.values = values;
    }

    @Override
    public T getValueForProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        if (progress < getStartDelayProgress()) {
            return values.get(0);
        } else if (progress >= 1f) {
            return values.get(values.size() - 1);
        }

        int keyframeIndex = getKeyframeIndex();

        float startKeytime = keyTimes.get(keyframeIndex);
        float endKeytime = keyTimes.get(keyframeIndex + 1);

        float percentageIntoFrame = 0;
        if (!isDiscrete) {
            percentageIntoFrame = (progress - startKeytime) / (endKeytime - startKeytime);
            if (interpolators != null) {
                percentageIntoFrame = interpolators.get(keyframeIndex).getInterpolation(percentageIntoFrame);
            }
        }

        Number startValue = values.get(keyframeIndex);
        Number endValue = values.get(keyframeIndex + 1);

        return klass.cast(lerp(startValue.floatValue(), endValue.floatValue(), percentageIntoFrame));
    }
}
