package com.airbnb.lottie.animation;

import com.airbnb.lottie.utils.LottieKeyframeAnimation;
import com.airbnb.lottie.utils.Observable;

@SuppressWarnings("WeakerAccess")
public interface AnimatableValue<T> {

    LottieKeyframeAnimation animationForKeyPath();
    boolean hasAnimation();
    Observable<T> getObservable();
}
