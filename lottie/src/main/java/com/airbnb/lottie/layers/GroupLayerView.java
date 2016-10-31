package com.airbnb.lottie.layers;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.airbnb.lottie.model.CircleShape;
import com.airbnb.lottie.model.RectangleShape;
import com.airbnb.lottie.model.ShapeFill;
import com.airbnb.lottie.model.ShapeGroup;
import com.airbnb.lottie.model.ShapePath;
import com.airbnb.lottie.model.ShapeStroke;
import com.airbnb.lottie.model.ShapeTransform;
import com.airbnb.lottie.model.ShapeTrimPath;

import java.util.Collections;
import java.util.List;

class GroupLayerView extends AnimatableLayer {

    private final ShapeGroup shapeGroup;
    @Nullable private final ShapeTransform shapeTransform;

    GroupLayerView(ShapeGroup shapeGroup, @Nullable ShapeFill previousFill,
            @Nullable ShapeStroke previousStroke, @Nullable ShapeTrimPath previousTrimPath,
            @Nullable ShapeTransform previousTransform, long compDuration, Drawable.Callback callback) {
        super(compDuration, callback);
        this.shapeGroup = shapeGroup;
        shapeTransform = previousTransform;
        setupShapeGroupWithFill(previousFill, previousStroke, previousTrimPath);
    }

    private void setupShapeGroupWithFill(ShapeFill previousFill,
            ShapeStroke previousStroke, ShapeTrimPath previousTrimPath) {
        if (shapeTransform != null) {
            setBounds(shapeTransform.getCompBounds());
            setAnchorPoint(shapeTransform.getAnchor().getObservable());
            setPosition(shapeTransform.getPosition().getObservable());
            setAlpha(shapeTransform.getOpacity().getObservable());
            setTransform(shapeTransform.getScale().getObservable());
            setRotation(shapeTransform.getRotation().getObservable());
        }

        List<Object> reversedItems = shapeGroup.getItems();
        Collections.reverse(reversedItems);

        ShapeFill currentFill = previousFill;
        ShapeStroke currentStroke = previousStroke;
        ShapeTransform currentTransform = null;
        ShapeTrimPath currentTrim = previousTrimPath;

        for (int i = 0; i < reversedItems.size(); i++) {
            Object item = reversedItems.get(i);
            if (item instanceof ShapeTransform) {
                currentTransform = (ShapeTransform) item;
            } else if (item instanceof ShapeStroke) {
                currentStroke = (ShapeStroke) item;
            } else if (item instanceof ShapeFill) {
                currentFill = (ShapeFill) item;
            } else if (item instanceof ShapeTrimPath) {
                currentTrim = (ShapeTrimPath) item;
            } else if (item instanceof ShapePath) {
                ShapePath shapePath = (ShapePath) item;
                ShapeLayerView shapeLayer = new ShapeLayerView(shapePath, currentFill, currentStroke, currentTrim, currentTransform, compDuration, getCallback());
                addLayer(shapeLayer);
            } else if (item instanceof RectangleShape) {
                RectangleShape shapeRect = (RectangleShape) item;
                RectShapeLayer shapeLayer = new RectShapeLayer(shapeRect, currentFill, currentStroke, currentTransform, compDuration, getCallback());
                addLayer(shapeLayer);
            } else if (item instanceof CircleShape) {
                CircleShape shapeCircle = (CircleShape) item;
                EllipseShapeLayer shapeLayer = new EllipseShapeLayer(shapeCircle, currentFill, currentStroke, currentTrim, currentTransform, compDuration, getCallback());
                addLayer(shapeLayer);
            } else if (item instanceof ShapeGroup) {
                ShapeGroup shapeGroup = (ShapeGroup) item;
                GroupLayerView groupLayer = new GroupLayerView(shapeGroup, currentFill, currentStroke, currentTrim, currentTransform, compDuration, getCallback());
                addLayer(groupLayer);
            }

        }

        buildAnimation();
    }

    private void buildAnimation() {
        if (shapeTransform != null) {
            addAnimation(shapeTransform.createAnimation());
        }
    }
}
