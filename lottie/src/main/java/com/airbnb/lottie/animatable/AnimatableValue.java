package com.airbnb.lottie.animatable;

import com.airbnb.lottie.animation.KeyframeAnimation;

@SuppressWarnings("WeakerAccess")
public interface AnimatableValue<T> {

    KeyframeAnimation animationForKeyPath();
    boolean hasAnimation();
    Observable<T> getObservable();
}
