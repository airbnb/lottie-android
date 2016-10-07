package com.airbnb.lotte.layers;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.airbnb.lotte.L;
import com.airbnb.lotte.animation.LotteAnimatableValue;
import com.airbnb.lotte.animation.LotteAnimationGroup;
import com.airbnb.lotte.model.LotteComposition;

import java.util.HashSet;
import java.util.Set;

class LotteParentLayer extends LotteAnimatableLayer {
    private static final String TAG = LotteParentLayer.class.getSimpleName();

    private LotteLayer parentModel;

    LotteParentLayer(LotteLayer parent, LotteComposition composition, Drawable.Callback callback) {
        super(composition.getDuration(), callback);
        setBounds(parent.getCompBounds());
        this.parentModel = parent;
        setupLayerFromModel();
        if (L.DBG) Log.d(TAG, "Creating parentModel layer for " + parent.getLayerName());
    }

    private void setupLayerFromModel() {
        setPosition(parentModel.getPosition().getObservable());
        setAnchorPoint(parentModel.getAnchor().getObservable());
        setTransform(parentModel.getScale().getObservable());
        setSublayerTransform(parentModel.getRotation().getObservable());
        buildAnimations();
    }

    private void buildAnimations() {
        Set<LotteAnimatableValue> propertyAnimations = new HashSet<>();
        propertyAnimations.add(parentModel.getPosition());
        propertyAnimations.add(parentModel.getAnchor());
        propertyAnimations.add(parentModel.getScale());
        propertyAnimations.add(parentModel.getRotation());
        addAnimation(new LotteAnimationGroup(propertyAnimations));
    }
}
