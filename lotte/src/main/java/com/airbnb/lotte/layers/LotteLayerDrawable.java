package com.airbnb.lotte.layers;

import com.airbnb.lotte.model.LotteAnimationGroup;
import com.airbnb.lotte.model.LotteComposition;
import com.airbnb.lotte.model.LotteKeyframeAnimation;

import java.util.List;

public class LotteLayerDrawable extends LotteAnimatableDrawable {

    private final LotteLayer layer;
    private final LotteComposition composition;

    private List<LotteGroupLayerDrawable> shapeLayers;
    private LotteLayer childContainerLayer;
    private LotteLayer rotationLayer;
    private LotteAnimationGroup animation;
    private LotteKeyframeAnimation inOutAnimation;
    private List<LotteParentLayer> parentLayers;
    private LotteMaskLayer maskLayer;
    private LotteLayerDrawable mask;


    public LotteLayerDrawable(LotteLayer layer, LotteComposition composition) {
        this.layer = layer;
        this.composition = composition;
    }

    private void setupForModel() {
        setBounds(composition.getBounds());
    }

    public long getId() {
        return layer.getId();
    }

    public LotteMaskLayer getMaskLayer() {
        return maskLayer;
    }

    public void setMaskLayer(LotteMaskLayer maskLayer) {
        this.maskLayer = maskLayer;
    }

    public void setMask(LotteLayerDrawable mask) {
        this.mask = mask;
    }
}
