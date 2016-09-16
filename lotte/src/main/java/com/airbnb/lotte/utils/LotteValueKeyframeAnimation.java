package com.airbnb.lotte.utils;

import android.support.annotation.FloatRange;

import java.util.List;

public class LotteValueKeyframeAnimation extends LotteKeyframeAnimation<Float> {

    private final List<Float> values;

    public LotteValueKeyframeAnimation(String objectProperty, long duration, List<Float> keyTimes, List<Float> values) {
        super(objectProperty, duration, keyTimes);
        if (keyTimes.size() != values.size()) {
            throw new IllegalArgumentException("Key times and values must be the same length " + keyTimes.size() + " vs " + values.size());
        }
        this.values = values;
    }

    @Override
    public Float getValueForProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        if (progress < startDelay) {
            return values.get(0);
        } else if (progress > startDelay + duration) {
            return values.get(values.size() - 1);
        }

        int keyframeIndex = getKeyframeIndex();

        float startKeytime = keyTimes.get(keyframeIndex);
        float endKeytime = keyTimes.get(keyframeIndex + 1);

        float percentageIntoFrame = (progress - startKeytime) / (endKeytime - startKeytime);
        if (interpolators != null) {
            percentageIntoFrame = interpolators.get(keyframeIndex - 1).getInterpolation(percentageIntoFrame);
        }

        float startValue = values.get(keyframeIndex - 1);
        float endValue = values.get(keyframeIndex);

        return startValue + percentageIntoFrame * (endValue - startValue);
    }
}
