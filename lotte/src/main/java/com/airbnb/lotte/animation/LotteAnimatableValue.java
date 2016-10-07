package com.airbnb.lotte.animation;

import com.airbnb.lotte.utils.LotteKeyframeAnimation;
import com.airbnb.lotte.utils.Observable;

public interface LotteAnimatableValue<T> {

    LotteKeyframeAnimation animationForKeyPath();
    boolean hasAnimation();
    Observable<T> getObservable();
}
