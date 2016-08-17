package com.airbnb.lotte.layers;

import android.util.Log;

import com.airbnb.lotte.L;
import com.airbnb.lotte.model.LotteComposition;
import com.airbnb.lotte.utils.LotteAnimationGroup;
import com.airbnb.lotte.utils.LotteTransform3D;

public class LotteParentLayer extends LotteAnimatableLayer {
    private static final String TAG = LotteParentLayer.class.getSimpleName();

    private LotteLayer parent;
    private LotteAnimationGroup animation;

    public LotteParentLayer(LotteLayer parent, LotteComposition composition) {
        super(composition.getDuration());
        this.parent = parent;
        setupLayerFromModel();
        if (L.DBG) Log.d(TAG, "Creating parent layer for " + parent.toString());
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
