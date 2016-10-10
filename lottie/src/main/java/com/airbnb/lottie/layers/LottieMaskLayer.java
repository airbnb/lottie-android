package com.airbnb.lottie.layers;

import android.graphics.drawable.Drawable;

import com.airbnb.lottie.model.LottieComposition;
import com.airbnb.lottie.model.LottieMask;

import java.util.List;

class LottieMaskLayer extends LottieAnimatableLayer {

    private final List<LottieMask> masks;

    LottieMaskLayer(List<LottieMask> masks, LottieComposition composition, Drawable.Callback callback) {
        super(composition.getDuration(), callback);
        this.masks = masks;
    }

    List<LottieMask> getMasks() {
        return masks;
    }
}
