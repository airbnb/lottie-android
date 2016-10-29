package com.airbnb.lottie.animation;

import android.support.annotation.FloatRange;

import com.airbnb.lottie.utils.LottieKeyframeAnimation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnimationGroup {

    public static AnimationGroup forAnimatableValues(AnimatableValue... animatableValues) {
        List<LottieKeyframeAnimation> animations = new ArrayList<>(animatableValues.length);

        for (AnimatableValue animatableValue : animatableValues) {
            if (animatableValue.hasAnimation()) {
                LottieKeyframeAnimation animation = animatableValue.animationForKeyPath();
                animations.add(animation);
            }
        }
        return new AnimationGroup(animations);
    }

    public static AnimationGroup forKeyframeAnimations(LottieKeyframeAnimation... animations) {
        return new AnimationGroup(Arrays.asList(animations));
    }

    private final List<LottieKeyframeAnimation> animations;

    private AnimationGroup(List<LottieKeyframeAnimation> animations) {
        this.animations = animations;
    }

    public void setProgress(@FloatRange(from=0f, to=1f) float progress) {
        for (int i = 0; i < animations.size(); i++) {
            animations.get(i).setProgress(progress);
        }
    }
}
