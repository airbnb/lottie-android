package com.airbnb.lottie.animation;

import android.support.annotation.RestrictTo;
import android.view.animation.Interpolator;

import com.airbnb.lottie.model.LottieComposition;
import com.airbnb.lottie.utils.ScaleXY;

import java.util.List;

import static com.airbnb.lottie.utils.MiscUtils.lerp;

@RestrictTo(RestrictTo.Scope.GROUP_ID)
public class ScaleKeyframeAnimation extends KeyframeAnimation<ScaleXY> {
    private final ScaleXY outTransform = new ScaleXY();

    private final List<ScaleXY> transforms;

    public ScaleKeyframeAnimation(long duration, LottieComposition composition, List<Float> keyTimes, List<ScaleXY> transforms, List<Interpolator> interpolators) {
        super(duration, composition, keyTimes, interpolators);
        this.transforms = transforms;
    }

    @Override
    public ScaleXY getValue() {
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

        ScaleXY startTransform = transforms.get(keyframeIndex);
        ScaleXY endTransform = transforms.get(keyframeIndex + 1);

        outTransform.scale(
                lerp(startTransform.getScaleX(), endTransform.getScaleX(), percentageIntoFrame),
                lerp(startTransform.getScaleY(), endTransform.getScaleY(), percentageIntoFrame));
        return outTransform;
    }
}
