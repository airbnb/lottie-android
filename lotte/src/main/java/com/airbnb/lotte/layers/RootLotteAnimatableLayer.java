package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.support.annotation.NonNull;

public class RootLotteAnimatableLayer extends LotteAnimatableLayer {

    public RootLotteAnimatableLayer() {
        super(0);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        canvas.clipRect(getBounds());
    }
}
