package com.airbnb.lottie.animation;

import android.graphics.Paint;
import android.os.LocaleList;

import androidx.annotation.NonNull;

/**
 * Custom paint that doesn't set text locale.
 * It takes ~1ms on initialization and isn't needed so removing it speeds up
 * setComposition.
 */
public class LPaint extends Paint {
    public LPaint() {
    }

    public LPaint(int flags) {
        super(flags);
    }

    @Override
    public void setTextLocales(@NonNull LocaleList locales) {
        // Do nothing.
    }
}
