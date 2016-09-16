package com.airbnb.lotte.animation;

import com.airbnb.lotte.utils.LotteKeyframeAnimation;

public interface LotteAnimatableValue {

    LotteKeyframeAnimation animationForKeyPath(String keyPath);
    boolean hasAnimation();

}
