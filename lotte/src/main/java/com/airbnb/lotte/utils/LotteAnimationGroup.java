package com.airbnb.lotte.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AnimationSet;

public class LotteAnimationGroup extends AnimationSet {
    public LotteAnimationGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LotteAnimationGroup(boolean shareInterpolator) {
        super(shareInterpolator);
    }
}
