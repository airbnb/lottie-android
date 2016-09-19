package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.airbnb.lotte.utils.LotteTransform3D;
import com.airbnb.lotte.utils.Observable;

public class RootLotteAnimatableLayer extends LotteAnimatableLayer {
    public RootLotteAnimatableLayer(Drawable.Callback callback) {
        super(0, callback);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        transform = new Observable<>(new LotteTransform3D());
        super.draw(canvas);
        canvas.clipRect(getBounds());
    }
}
