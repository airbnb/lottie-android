package com.airbnb.lottie.animation;

import android.support.annotation.FloatRange;

import com.airbnb.lottie.utils.LotteKeyframeAnimation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LotteAnimationGroup {

    private final List<LotteKeyframeAnimation> animations;

    public LotteAnimationGroup(Set<LotteAnimatableValue> animations) {
        this.animations = new ArrayList<>(animations.size());

        for (LotteAnimatableValue animatableValue : animations) {
            if (animatableValue.hasAnimation()) {
                LotteKeyframeAnimation animation = animatableValue.animationForKeyPath();
                this.animations.add(animation);
            }
        }
    }

    public LotteAnimationGroup(List<LotteKeyframeAnimation> animations) {
        this.animations = animations;
    }

    public void setProgress(@FloatRange(from=0f, to=1f) float progress) {
        for (int i = 0; i < animations.size(); i++) {
            animations.get(i).setProgress(progress);
        }
    }
}
