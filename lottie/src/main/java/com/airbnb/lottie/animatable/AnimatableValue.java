package com.airbnb.lottie.animatable;

import com.airbnb.lottie.animation.KeyframeAnimation;

public interface AnimatableValue<T> {

    KeyframeAnimation animationForKeyPath();
    boolean hasAnimation();
    Observable<T> getObservable();
}
