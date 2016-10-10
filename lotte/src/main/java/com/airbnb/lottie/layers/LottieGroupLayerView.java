package com.airbnb.lottie.layers;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.airbnb.lottie.animation.LottieAnimatableValue;
import com.airbnb.lottie.animation.LottieAnimationGroup;
import com.airbnb.lottie.model.LottieShapeCircle;
import com.airbnb.lottie.model.LottieShapeFill;
import com.airbnb.lottie.model.LottieShapeGroup;
import com.airbnb.lottie.model.LottieShapePath;
import com.airbnb.lottie.model.LottieShapeRectangle;
import com.airbnb.lottie.model.LottieShapeStroke;
import com.airbnb.lottie.model.LottieShapeTransform;
import com.airbnb.lottie.model.LottieShapeTrimPath;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class LottieGroupLayerView extends LottieAnimatableLayer {

    private final LottieShapeGroup shapeGroup;
    @Nullable private final LottieShapeTransform shapeTransform;

    LottieGroupLayerView(LottieShapeGroup shapeGroup, @Nullable LottieShapeFill previousFill,
            @Nullable LottieShapeStroke previousStroke, @Nullable LottieShapeTrimPath previousTrimPath,
            @Nullable LottieShapeTransform previousTransform, long compDuration, Drawable.Callback callback) {
        super(compDuration, callback);
        this.shapeGroup = shapeGroup;
        shapeTransform = previousTransform;
        setupShapeGroupWithFill(previousFill, previousStroke, previousTrimPath);
    }

    private void setupShapeGroupWithFill(LottieShapeFill previousFill,
            LottieShapeStroke previousStroke, LottieShapeTrimPath previousTrimPath) {
        if (shapeTransform != null) {
            setBounds(shapeTransform.getCompBounds());
            setAnchorPoint(shapeTransform.getAnchor().getObservable());
            setPosition(shapeTransform.getPosition().getObservable());
            setAlpha(shapeTransform.getOpacity().getObservable());
            setTransform(shapeTransform.getScale().getObservable());
            setSublayerTransform(shapeTransform.getRotation().getObservable());
        }

        List<Object> reversedItems = shapeGroup.getItems();
        Collections.reverse(reversedItems);

        LottieShapeFill currentFill = previousFill;
        LottieShapeStroke currentStroke = previousStroke;
        LottieShapeTransform currentTransform = null;
        LottieShapeTrimPath currentTrim = previousTrimPath;

        for (int i = 0; i < reversedItems.size(); i++) {
            Object item = reversedItems.get(i);
            if (item instanceof LottieShapeTransform) {
                currentTransform = (LottieShapeTransform) item;
            } else if (item instanceof LottieShapeStroke) {
                currentStroke = (LottieShapeStroke) item;
            } else if (item instanceof LottieShapeFill) {
                currentFill = (LottieShapeFill) item;
            } else if (item instanceof LottieShapeTrimPath) {
                currentTrim = (LottieShapeTrimPath) item;
            } else if (item instanceof LottieShapePath) {
                LottieShapePath shapePath = (LottieShapePath) item;
                LottieShapeLayerView shapeLayer = new LottieShapeLayerView(shapePath, currentFill, currentStroke, currentTrim, currentTransform, compDuration, getCallback());
                addLayer(shapeLayer);
            } else if (item instanceof LottieShapeRectangle) {
                LottieShapeRectangle shapeRect = (LottieShapeRectangle) item;
                LottieRectShapeLayer shapeLayer = new LottieRectShapeLayer(shapeRect, currentFill, currentStroke, currentTransform, compDuration, getCallback());
                addLayer(shapeLayer);
            } else if (item instanceof LottieShapeCircle) {
                LottieShapeCircle shapeCircle = (LottieShapeCircle) item;
                LottieEllipseShapeLayer shapeLayer = new LottieEllipseShapeLayer(shapeCircle, currentFill, currentStroke, currentTrim, currentTransform, compDuration, getCallback());
                addLayer(shapeLayer);
            } else if (item instanceof LottieShapeGroup) {
                LottieShapeGroup shapeGroup = (LottieShapeGroup) item;
                LottieGroupLayerView groupLayer = new LottieGroupLayerView(shapeGroup, currentFill, currentStroke, currentTrim, currentTransform, compDuration, getCallback());
                addLayer(groupLayer);
            }

        }

        buildAnimation();
    }

    private void buildAnimation() {
        Set<LottieAnimatableValue> propertyAnimations = new HashSet<>();
        if (shapeTransform != null) {
            propertyAnimations.add(shapeTransform.getOpacity());
            propertyAnimations.add(shapeTransform.getPosition());
            propertyAnimations.add(shapeTransform.getAnchor());
            propertyAnimations.add(shapeTransform.getScale());
            propertyAnimations.add(shapeTransform.getRotation());
        }
        addAnimation(new LottieAnimationGroup(propertyAnimations));
    }
}
