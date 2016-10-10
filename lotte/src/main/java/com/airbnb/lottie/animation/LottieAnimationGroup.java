package com.airbnb.lottie.animation;

import android.support.annotation.FloatRange;

import com.airbnb.lottie.utils.LottieKeyframeAnimation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LottieAnimationGroup {

    private final List<LottieKeyframeAnimation> animations;

    public LottieAnimationGroup(Set<LottieAnimatableValue> animations) {
        this.animations = new ArrayList<>(animations.size());

        for (LottieAnimatableValue animatableValue : animations) {
            if (animatableValue.hasAnimation()) {
                LottieKeyframeAnimation animation = animatableValue.animationForKeyPath();
                this.animations.add(animation);
            }
        }
    }

    public LottieAnimationGroup(List<LottieKeyframeAnimation> animations) {
        this.animations = animations;
    }

    public void setProgress(@FloatRange(from=0f, to=1f) float progress) {
        for (int i = 0; i < animations.size(); i++) {
            animations.get(i).setProgress(progress);
        }
    }
}
