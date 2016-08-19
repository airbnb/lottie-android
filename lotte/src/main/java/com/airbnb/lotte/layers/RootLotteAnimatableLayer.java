package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.support.annotation.NonNull;

import com.airbnb.lotte.utils.LotteTransform3D;

public class RootLotteAnimatableLayer extends LotteAnimatableLayer {

    public RootLotteAnimatableLayer() {
        super(0);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        transform = new LotteTransform3D();
        super.draw(canvas);
        canvas.clipRect(getBounds());
    }
}
