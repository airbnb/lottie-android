package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public class LotteAnimatableDrawable extends Drawable {

    private long duration;

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public void draw(Canvas canvas) {

    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
