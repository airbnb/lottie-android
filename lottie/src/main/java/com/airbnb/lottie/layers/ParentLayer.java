package com.airbnb.lottie.layers;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.model.LottieComposition;
import com.airbnb.lottie.model.Layer;

class ParentLayer extends AnimatableLayer {
    private static final String TAG = ParentLayer.class.getSimpleName();

    private final Layer parentModel;

    ParentLayer(Layer parent, LottieComposition composition, Drawable.Callback callback) {
        super(composition.getDuration(), callback);
        setBounds(parent.getComposition().getBounds());
        this.parentModel = parent;
        setupLayerFromModel();
        if (L.DBG) Log.d(TAG, "Creating parentModel layer for " + parent.getLayerName());
    }

    private void setupLayerFromModel() {
        setPosition(parentModel.getPosition().getObservable());
        setAnchorPoint(parentModel.getAnchor().getObservable());
        setTransform(parentModel.getScale().getObservable());
        setRotation(parentModel.getRotation().getObservable());
        buildAnimations();
    }

    private void buildAnimations() {
        addAnimation(parentModel.createAnimation());
    }
}
