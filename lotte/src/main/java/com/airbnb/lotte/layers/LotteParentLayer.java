package com.airbnb.lotte.layers;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.SparseArray;

import com.airbnb.lotte.L;
import com.airbnb.lotte.animation.LotteAnimatableProperty;
import com.airbnb.lotte.animation.LotteAnimatableValue;
import com.airbnb.lotte.animation.LotteAnimationGroup;
import com.airbnb.lotte.model.LotteComposition;
import com.airbnb.lotte.utils.LotteTransform3D;
import com.airbnb.lotte.utils.Observable;

public class LotteParentLayer extends LotteAnimatableLayer {
    private static final String TAG = LotteParentLayer.class.getSimpleName();

    private LotteLayer parentModel;
    private LotteAnimationGroup animation;

    public LotteParentLayer(LotteLayer parent, LotteComposition composition, Drawable.Callback callback) {
        super(composition.getDuration(), callback);
        setBounds(parent.getCompBounds());
        this.parentModel = parent;
        setupLayerFromModel();
        if (L.DBG) Log.d(TAG, "Creating parentModel layer for " + parent.getLayerName());
    }

    private void setupLayerFromModel() {
        setPosition(parentModel.getPosition().getObservable());
        anchorPoint = parentModel.getAnchor().getObservable();
        setTransform(parentModel.getScale().getObservable());
        sublayerTransform = new Observable<>(new LotteTransform3D());
        sublayerTransform.getValue().rotateZ(parentModel.getRotation().getInitialValue());
        buildAnimations();
    }

    private void buildAnimations() {
        SparseArray<LotteAnimatableValue> propertyAnimations = new SparseArray<>();
        propertyAnimations.put(LotteAnimatableProperty.POSITION, parentModel.getPosition());
        propertyAnimations.put(LotteAnimatableProperty.ANCHOR_POINT, parentModel.getAnchor());
        propertyAnimations.put(LotteAnimatableProperty.TRANSFORM, parentModel.getScale());
        propertyAnimations.put(LotteAnimatableProperty.SUBLAYER_TRANSFORM, parentModel.getRotation());
        addAnimation(new LotteAnimationGroup(propertyAnimations));
    }
}
