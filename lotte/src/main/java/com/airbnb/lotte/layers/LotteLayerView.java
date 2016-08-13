package com.airbnb.lotte.layers;

import com.airbnb.lotte.model.LotteComposition;
import com.airbnb.lotte.model.LotteKeyframeAnimation;
import com.airbnb.lotte.utils.LotteAnimationGroup;

import java.util.List;

public class LotteLayerView extends LotteAnimatableLayer {

    private final LotteLayer layer;
    private final LotteComposition composition;

    private List<LotteGroupLayerDrawable> shapeLayers;
    private LotteLayer childContainerLayer;
    private LotteLayer rotationLayer;
    private LotteAnimationGroup animation;
    private LotteKeyframeAnimation inOutAnimation;
    private List<LotteParentLayer> parentLayers;
    private LotteMaskLayer maskLayer;
    private LotteLayerView mask;


    public LotteLayerView(LotteLayer layer, LotteComposition composition) {
        super(composition.getDuration());
        this.layer = layer;
        this.composition = composition;
        setBounds(composition.getBounds());
        setupForModel();
    }

    private void setupForModel() {
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

    public void setMask(LotteLayerView mask) {
        this.mask = mask;
    }
}
