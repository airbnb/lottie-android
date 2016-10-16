package com.airbnb.lottie.layers;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.model.LottieComposition;

class LottieParentLayer extends LottieAnimatableLayer {
    private static final String TAG = LottieParentLayer.class.getSimpleName();

    private final LottieLayer parentModel;

    LottieParentLayer(LottieLayer parent, LottieComposition composition, Drawable.Callback callback) {
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
        addAnimation(parentModel.createAnimation());
    }
}
