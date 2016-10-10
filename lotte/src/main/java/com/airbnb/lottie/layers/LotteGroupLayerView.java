package com.airbnb.lottie.layers;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.airbnb.lottie.animation.LotteAnimatableValue;
import com.airbnb.lottie.animation.LotteAnimationGroup;
import com.airbnb.lottie.model.LotteShapeCircle;
import com.airbnb.lottie.model.LotteShapeFill;
import com.airbnb.lottie.model.LotteShapeGroup;
import com.airbnb.lottie.model.LotteShapePath;
import com.airbnb.lottie.model.LotteShapeRectangle;
import com.airbnb.lottie.model.LotteShapeStroke;
import com.airbnb.lottie.model.LotteShapeTransform;
import com.airbnb.lottie.model.LotteShapeTrimPath;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class LotteGroupLayerView extends LotteAnimatableLayer {

    private final LotteShapeGroup shapeGroup;
    @Nullable private final LotteShapeTransform shapeTransform;

    LotteGroupLayerView(LotteShapeGroup shapeGroup, @Nullable LotteShapeFill previousFill,
            @Nullable LotteShapeStroke previousStroke, @Nullable LotteShapeTrimPath previousTrimPath,
            @Nullable LotteShapeTransform previousTransform, long compDuration, Drawable.Callback callback) {
        super(compDuration, callback);
        this.shapeGroup = shapeGroup;
        shapeTransform = previousTransform;
        setupShapeGroupWithFill(previousFill, previousStroke, previousTrimPath);
    }

    private void setupShapeGroupWithFill(LotteShapeFill previousFill,
            LotteShapeStroke previousStroke, LotteShapeTrimPath previousTrimPath) {
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

        LotteShapeFill currentFill = previousFill;
        LotteShapeStroke currentStroke = previousStroke;
        LotteShapeTransform currentTransform = null;
        LotteShapeTrimPath currentTrim = previousTrimPath;

        for (int i = 0; i < reversedItems.size(); i++) {
            Object item = reversedItems.get(i);
            if (item instanceof LotteShapeTransform) {
                currentTransform = (LotteShapeTransform) item;
            } else if (item instanceof LotteShapeStroke) {
                currentStroke = (LotteShapeStroke) item;
            } else if (item instanceof LotteShapeFill) {
                currentFill = (LotteShapeFill) item;
            } else if (item instanceof LotteShapeTrimPath) {
                currentTrim = (LotteShapeTrimPath) item;
            } else if (item instanceof LotteShapePath) {
                LotteShapePath shapePath = (LotteShapePath) item;
                LotteShapeLayerView shapeLayer = new LotteShapeLayerView(shapePath, currentFill, currentStroke, currentTrim, currentTransform, compDuration, getCallback());
                addLayer(shapeLayer);
            } else if (item instanceof LotteShapeRectangle) {
                LotteShapeRectangle shapeRect = (LotteShapeRectangle) item;
                LotteRectShapeLayer shapeLayer = new LotteRectShapeLayer(shapeRect, currentFill, currentStroke, currentTransform, compDuration, getCallback());
                addLayer(shapeLayer);
            } else if (item instanceof LotteShapeCircle) {
                LotteShapeCircle shapeCircle = (LotteShapeCircle) item;
                LotteEllipseShapeLayer shapeLayer = new LotteEllipseShapeLayer(shapeCircle, currentFill, currentStroke, currentTrim, currentTransform, compDuration, getCallback());
                addLayer(shapeLayer);
            } else if (item instanceof LotteShapeGroup) {
                LotteShapeGroup shapeGroup = (LotteShapeGroup) item;
                LotteGroupLayerView groupLayer = new LotteGroupLayerView(shapeGroup, currentFill, currentStroke, currentTrim, currentTransform, compDuration, getCallback());
                addLayer(groupLayer);
            }

        }

        buildAnimation();
    }

    private void buildAnimation() {
        Set<LotteAnimatableValue> propertyAnimations = new HashSet<>();
        if (shapeTransform != null) {
            propertyAnimations.add(shapeTransform.getOpacity());
            propertyAnimations.add(shapeTransform.getPosition());
            propertyAnimations.add(shapeTransform.getAnchor());
            propertyAnimations.add(shapeTransform.getScale());
            propertyAnimations.add(shapeTransform.getRotation());
        }
        addAnimation(new LotteAnimationGroup(propertyAnimations));
    }
}
