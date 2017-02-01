package com.airbnb.lottie.animation;

import android.support.annotation.FloatRange;
import android.support.annotation.RestrictTo;
import android.view.animation.Interpolator;

import java.util.Collections;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class StaticKeyframeAnimation<T> extends KeyframeAnimation<T> {


    private final T initialValue;

    public StaticKeyframeAnimation(T initialValue) {
        super(0, null, Collections.<Float>emptyList(), Collections.<Interpolator>emptyList());
        this.initialValue = initialValue;
    }

    @Override
    public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        // Do nothing
    }

    @Override
    public T getValue() {
        return initialValue;
    }
}
