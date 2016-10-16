package com.airbnb.lottie.animation;

import android.support.annotation.FloatRange;

import com.airbnb.lottie.utils.LottieKeyframeAnimation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LottieAnimationGroup {

    public static LottieAnimationGroup forAnimatableValues(LottieAnimatableValue... animatableValues) {
        List<LottieKeyframeAnimation> animations = new ArrayList<>(animatableValues.length);

        for (LottieAnimatableValue animatableValue : animatableValues) {
            if (animatableValue.hasAnimation()) {
                LottieKeyframeAnimation animation = animatableValue.animationForKeyPath();
                animations.add(animation);
            }
        }
        return new LottieAnimationGroup(animations);
    }

    public static LottieAnimationGroup forKeyframeAnimations(LottieKeyframeAnimation... animations) {
        return new LottieAnimationGroup(Arrays.asList(animations));
    }

    private final List<LottieKeyframeAnimation> animations;

    private LottieAnimationGroup(List<LottieKeyframeAnimation> animations) {
        this.animations = animations;
    }

    public void setProgress(@FloatRange(from=0f, to=1f) float progress) {
        for (int i = 0; i < animations.size(); i++) {
            animations.get(i).setProgress(progress);
        }
    }
}
