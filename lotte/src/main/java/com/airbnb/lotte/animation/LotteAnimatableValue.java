package com.airbnb.lotte.animation;

import com.airbnb.lotte.animation.LotteAnimatableProperty.AnimatableProperty;
import com.airbnb.lotte.utils.LotteKeyframeAnimation;

public interface LotteAnimatableValue {

    LotteKeyframeAnimation animationForKeyPath(@AnimatableProperty int property);
    boolean hasAnimation();

}
