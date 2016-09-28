package com.airbnb.lotte.animation;

import android.support.annotation.FloatRange;
import android.util.SparseArray;

import com.airbnb.lotte.animation.LotteAnimatableProperty.AnimatableProperty;
import com.airbnb.lotte.utils.LotteKeyframeAnimation;

import java.util.ArrayList;
import java.util.List;

public class LotteAnimationGroup {

    private final List<LotteKeyframeAnimation> animations;

    public LotteAnimationGroup(SparseArray<LotteAnimatableValue> propertyAnimations) {
        animations = new ArrayList<>(propertyAnimations.size());

        for (int i = 0; i < propertyAnimations.size(); i++) {
            @AnimatableProperty int property = propertyAnimations.keyAt(i);
            LotteAnimatableValue animatableValue = propertyAnimations.get(property);
            if (animatableValue.hasAnimation()) {
                LotteKeyframeAnimation animation = animatableValue.animationForKeyPath(property);
                animations.add(animation);
            }
        }
    }

    public LotteAnimationGroup(List<LotteKeyframeAnimation> animations) {
        this.animations = animations;
    }

    public void play() {
        for (LotteKeyframeAnimation animation : animations) {
            animation.play();
        }
    }

    public void setProgress(@FloatRange(from=0f, to=1f) float progress) {
        for (LotteKeyframeAnimation animation : animations) {
            animation.setProgress(progress);
        }
    }

    public void setMaxDuration(long maxDuration) {
        for (LotteKeyframeAnimation<?> animation : animations) {
            animation.setTotalDuration(maxDuration);
        }
    }
}
