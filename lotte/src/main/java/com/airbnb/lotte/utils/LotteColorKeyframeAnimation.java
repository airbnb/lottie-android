package com.airbnb.lotte.utils;

import android.graphics.Color;
import android.support.annotation.FloatRange;

import com.airbnb.lotte.animation.LotteAnimatableProperty.AnimatableProperty;

import java.util.List;


public class LotteColorKeyframeAnimation extends LotteKeyframeAnimation<Integer> {
    private final float[] hsv1 = new float[3];
    private final float[] hsv2 = new float[3];

    private final List<Integer> values;

    public LotteColorKeyframeAnimation(@AnimatableProperty int property, long duration, List<Float> keyTimes, List<Integer> values) {
        super(property, duration, keyTimes);
        if (keyTimes.size() != values.size()) {
            throw new IllegalArgumentException("Key times and values must be the same length " + keyTimes.size() + " vs " + values.size());
        }
        this.values = values;
    }

    @Override
    public Integer getValueForProgress(@FloatRange(from = 0f, to = 1f) float progress) {
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

        int startColor = values.get(keyframeIndex);
        int endColor = values.get(keyframeIndex + 1);

        Color.colorToHSV(startColor, hsv1);
        Color.colorToHSV(endColor, hsv2);

        int a = (int) MiscUtils.lerp(Color.alpha(startColor), Color.alpha(endColor), percentageIntoFrame);
        float h = MiscUtils.lerp(hsv1[0], hsv2[0], percentageIntoFrame);
        float s = MiscUtils.lerp(hsv1[1], hsv2[1], percentageIntoFrame);
        float v = MiscUtils.lerp(hsv1[2], hsv2[2], percentageIntoFrame);
        hsv1[0] = h;
        hsv1[1] = s;
        hsv1[2] = v;
        return Color.HSVToColor(a, hsv1);
    }
}
