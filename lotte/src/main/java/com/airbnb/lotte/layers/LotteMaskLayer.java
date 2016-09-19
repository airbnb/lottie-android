package com.airbnb.lotte.layers;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.airbnb.lotte.model.LotteComposition;
import com.airbnb.lotte.model.LotteMask;

import java.util.List;

public class LotteMaskLayer extends LotteAnimatableLayer {

    private final List<LotteMask> masks;
    private final LotteComposition composition;

    public LotteMaskLayer(List<LotteMask> masks, LotteComposition composition, Drawable.Callback callback) {
        super(composition.getDuration(), callback);
        this.masks = masks;
        this.composition = composition;
    }

    private void setupViewFromModel() {
        for (LotteMask mask : masks) {
            LotteShapeLayer maskLayer = new LotteShapeLayer();
            maskLayer.setPath(mask.getMaskPath().getInitialShape());
            maskLayer.setColor(Color.WHITE);
            maskLayer.setAlpha((int) (mask.getOpacity().getInitialValue()));
            addLayer(maskLayer);

            // TODO: animations
        }
    }

    public List<LotteMask> getMasks() {
        return masks;
    }
}
