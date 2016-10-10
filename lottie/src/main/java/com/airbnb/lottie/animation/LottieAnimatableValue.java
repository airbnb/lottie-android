package com.airbnb.lottie.animation;

import com.airbnb.lottie.utils.LottieKeyframeAnimation;
import com.airbnb.lottie.utils.Observable;

public interface LottieAnimatableValue<T> {

    LottieKeyframeAnimation animationForKeyPath();
    boolean hasAnimation();
    Observable<T> getObservable();
}
