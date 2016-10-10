package com.airbnb.lottie.layers;

import android.graphics.drawable.Drawable;

import com.airbnb.lottie.model.LotteComposition;
import com.airbnb.lottie.model.LotteMask;

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
