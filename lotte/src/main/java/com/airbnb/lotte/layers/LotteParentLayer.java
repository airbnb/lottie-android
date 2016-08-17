package com.airbnb.lotte.layers;

import com.airbnb.lotte.model.LotteComposition;
import com.airbnb.lotte.utils.LotteAnimationGroup;
import com.airbnb.lotte.utils.LotteTransform3D;

public class LotteParentLayer extends LotteAnimatableLayer {

    private LotteLayer parent;
    private LotteAnimationGroup animation;

    public LotteParentLayer(LotteLayer parent, LotteComposition composition) {
        super(composition.getDuration());
        this.parent = parent;
        setupLayerFromModel();
    }

    private void setupLayerFromModel() {
        position = parent.getPosition().getInitialPoint();
        anchorPoint = parent.getAnchor().getInitialPoint();
        transform = parent.getScale().getInitialScale();
        sublayerTransform = new LotteTransform3D();
        sublayerTransform.rotateZ(parent.getRotation().getInitialValue());
        buildAnimations();
    }

    private void buildAnimations() {
        // TODO
    }
}
