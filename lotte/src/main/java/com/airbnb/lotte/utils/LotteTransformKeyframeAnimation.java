package com.airbnb.lotte.utils;

import android.support.annotation.FloatRange;

import com.airbnb.lotte.animation.LotteAnimatableProperty.AnimatableProperty;

import java.util.List;

import static com.airbnb.lotte.utils.MiscUtils.lerp;

public class LotteTransformKeyframeAnimation extends LotteKeyframeAnimation<LotteTransform3D> {
    private final LotteTransform3D outTransform = new LotteTransform3D();

    private final List<LotteTransform3D> transforms;

    public LotteTransformKeyframeAnimation(@AnimatableProperty int property, long duration, List<Float> keyTimes, List<LotteTransform3D> transforms) {
        super(property, duration, keyTimes);
        this.transforms = transforms;
    }

    @Override
    public LotteTransform3D getValueForProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        if (progress <= 0f) {
            return transforms.get(0);
        } else if (progress >= 1f) {
            return transforms.get(transforms.size() - 1);
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

        LotteTransform3D startTransform = transforms.get(keyframeIndex);
        LotteTransform3D endTransform = transforms.get(keyframeIndex + 1);

        outTransform.scale(
                lerp(startTransform.getScaleX(), endTransform.getScaleX(), percentageIntoFrame),
                lerp(startTransform.getScaleY(), endTransform.getScaleY(), percentageIntoFrame));
        outTransform.rotateZ(lerp(startTransform.getRotationZ(), endTransform.getRotationZ(), percentageIntoFrame));
        return outTransform;
    }
}
