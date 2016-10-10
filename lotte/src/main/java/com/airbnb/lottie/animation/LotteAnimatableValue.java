package com.airbnb.lottie.animation;

import com.airbnb.lottie.utils.LotteKeyframeAnimation;
import com.airbnb.lottie.utils.Observable;

public interface LotteAnimatableValue<T> {

    LotteKeyframeAnimation animationForKeyPath();
    boolean hasAnimation();
    Observable<T> getObservable();
}
