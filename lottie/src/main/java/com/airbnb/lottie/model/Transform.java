package com.airbnb.lottie.model;

import android.graphics.Rect;
import android.support.annotation.RestrictTo;

import com.airbnb.lottie.animatable.AnimatableFloatValue;
import com.airbnb.lottie.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.animatable.AnimatablePathValue;
import com.airbnb.lottie.animatable.AnimatableScaleValue;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface Transform {
    Rect getBounds();

    AnimatablePathValue getPosition();

    AnimatablePathValue getAnchor();

    AnimatableScaleValue getScale();

    AnimatableFloatValue getRotation();

    AnimatableIntegerValue getOpacity();
}