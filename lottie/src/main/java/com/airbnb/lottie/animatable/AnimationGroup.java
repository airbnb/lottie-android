package com.airbnb.lottie.animatable;

import android.support.annotation.FloatRange;

import com.airbnb.lottie.animation.KeyframeAnimation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnimationGroup {

    public static AnimationGroup forAnimatableValues(AnimatableValue... animatableValues) {
        List<KeyframeAnimation> animations = new ArrayList<>(animatableValues.length);

        for (AnimatableValue animatableValue : animatableValues) {
            if (animatableValue.hasAnimation()) {
                KeyframeAnimation animation = animatableValue.animationForKeyPath();
                animations.add(animation);
            }
        }
        return new AnimationGroup(animations);
    }

    public static AnimationGroup forKeyframeAnimations(KeyframeAnimation... animations) {
        return new AnimationGroup(Arrays.asList(animations));
    }

    private final List<KeyframeAnimation> animations;

    private AnimationGroup(List<KeyframeAnimation> animations) {
        this.animations = animations;
    }

    public void setProgress(@FloatRange(from=0f, to=1f) float progress) {
        for (int i = 0; i < animations.size(); i++) {
            animations.get(i).setProgress(progress);
        }
    }
}
