package com.airbnb.lotte.model;

import com.airbnb.lotte.utils.LotteKeyframeAnimation;

public interface LotteAnimatableValue {

    LotteKeyframeAnimation animationForKeyPath(String keyPath);
    boolean hasAnimation();

}
