package com.airbnb.lotte.layers;

import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

public class LotteAnimatableLayer extends Drawable {

    /** This should mimic CALayer#position */
    protected Point position;
    /** This should mimic CALayer#anchorPosition */
    protected Point anchorPosition;
    /** This should mimic CALayer#transform */
    protected Camera transform;
    /** This should mimic CALayer#sublayerTransform */
    protected Camera sublayerTransform;
    protected long duration;

    public LotteAnimatableLayer(long duration) {
        this.duration = duration;
    }

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
