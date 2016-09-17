package com.airbnb.lotte.animation;

import com.airbnb.lotte.animation.LotteAnimatableProperty.AnimatableProperty;
import com.airbnb.lotte.utils.LotteKeyframeAnimation;
import com.airbnb.lotte.utils.Observable;

public interface LotteAnimatableValue<T> {

    LotteKeyframeAnimation animationForKeyPath(@AnimatableProperty int property);
    boolean hasAnimation();
    Observable<T> getObservable();
}
