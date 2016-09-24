package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public class RootLotteAnimatableLayer extends LotteAnimatableLayer {
    public RootLotteAnimatableLayer(Drawable.Callback callback) {
        super(0, callback);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        canvas.clipRect(getBounds());
    }
}
