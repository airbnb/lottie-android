package com.airbnb.lotte.layers;

import android.graphics.drawable.Drawable;

import com.airbnb.lotte.model.LotteComposition;
import com.airbnb.lotte.model.LotteMask;

import java.util.List;

class LotteMaskLayer extends LotteAnimatableLayer {

    private final List<LotteMask> masks;

    LotteMaskLayer(List<LotteMask> masks, LotteComposition composition, Drawable.Callback callback) {
        super(composition.getDuration(), callback);
        this.masks = masks;
    }

    List<LotteMask> getMasks() {
        return masks;
    }
}
