package com.airbnb.lotte.layers;

import android.util.Log;

import com.airbnb.lotte.L;
import com.airbnb.lotte.model.LotteComposition;
import com.airbnb.lotte.utils.LotteAnimationGroup;
import com.airbnb.lotte.utils.LotteTransform3D;

public class LotteParentLayer extends LotteAnimatableLayer {
    private static final String TAG = LotteParentLayer.class.getSimpleName();

    private LotteLayer parentModel;
    private LotteAnimationGroup animation;

    public LotteParentLayer(LotteLayer parent, LotteComposition composition) {
        super(composition.getDuration());
        setBounds(parent.getCompBounds());
        this.parentModel = parent;
        setupLayerFromModel();
        if (L.DBG) Log.d(TAG, "Creating parentModel layer for " + parent.toString());
    }

    private void setupLayerFromModel() {
        position = parentModel.getPosition().getInitialPoint();
        anchorPoint = parentModel.getAnchor().getInitialPoint();
        transform = parentModel.getScale().getInitialScale();
        sublayerTransform = new LotteTransform3D();
        sublayerTransform.rotateZ(parentModel.getRotation().getInitialValue());
        buildAnimations();
    }

    private void buildAnimations() {
        // TODO
    }
}
