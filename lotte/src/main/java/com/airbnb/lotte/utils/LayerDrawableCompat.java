package com.airbnb.lotte.utils;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class LayerDrawableCompat extends Drawable {

    private List<Drawable> drawables = new ArrayList<>(2);

    public void addDrawable(Drawable drawable) {
        drawables.add(drawable);
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        for (Drawable d : drawables) {
            d.draw(canvas);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        for (Drawable d : drawables) {
            d.setAlpha(alpha);
        }
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        for (Drawable d : drawables) {
            d.setColorFilter(colorFilter);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
