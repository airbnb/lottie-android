package com.airbnb.lottie.animation;

import android.view.animation.Interpolator;

import com.airbnb.lottie.utils.LottieTransform3D;

import java.util.List;

import static com.airbnb.lottie.utils.MiscUtils.lerp;

public class TransformKeyframeAnimation extends KeyframeAnimation<LottieTransform3D> {
    private final LottieTransform3D outTransform = new LottieTransform3D();

    private final List<LottieTransform3D> transforms;

    public TransformKeyframeAnimation(long duration, long compDuration, List<Float> keyTimes, List<LottieTransform3D> transforms, List<Interpolator> interpolators) {
        super(duration, compDuration, keyTimes, interpolators);
        this.transforms = transforms;
    }

    @Override
    public LottieTransform3D getValue() {
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

        LottieTransform3D startTransform = transforms.get(keyframeIndex);
        LottieTransform3D endTransform = transforms.get(keyframeIndex + 1);

        outTransform.scale(
                lerp(startTransform.getScaleX(), endTransform.getScaleX(), percentageIntoFrame),
                lerp(startTransform.getScaleY(), endTransform.getScaleY(), percentageIntoFrame));
        outTransform.rotateZ(lerp(startTransform.getRotationZ(), endTransform.getRotationZ(), percentageIntoFrame));
        return outTransform;
    }
}
