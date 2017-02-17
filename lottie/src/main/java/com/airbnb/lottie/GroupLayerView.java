package com.airbnb.lottie;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class GroupLayerView extends AnimatableLayer {
  private final ShapeGroup shapeGroup;
  @Nullable private final Transform shapeTransform;

  GroupLayerView(ShapeGroup shapeGroup, @Nullable ShapeFill previousFill,
      @Nullable ShapeStroke previousStroke, @Nullable ShapeTrimPath previousTrimPath,
      @Nullable Transform previousTransform, Drawable.Callback callback) {
    super(callback);
    this.shapeGroup = shapeGroup;
    shapeTransform = previousTransform;
    setupShapeGroupWithFill(previousFill, previousStroke, previousTrimPath);
  }

  private void setupShapeGroupWithFill(ShapeFill previousFill,
      ShapeStroke previousStroke, ShapeTrimPath previousTrimPath) {
    if (shapeTransform != null) {
      setBounds(shapeTransform.getBounds());
      setAnchorPoint(shapeTransform.getAnchor().createAnimation());
      setPosition(shapeTransform.getPosition().createAnimation());
      setAlpha(shapeTransform.getOpacity().createAnimation());
      setTransform(shapeTransform.getScale().createAnimation());
      setRotation(shapeTransform.getRotation().createAnimation());
    }

    List<Object> reversedItems = new ArrayList<>(shapeGroup.getItems());
    Collections.reverse(reversedItems);

    ShapeFill currentFill = previousFill;
    ShapeStroke currentStroke = previousStroke;
    Transform currentTransform = null;
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
        ShapeLayerView shapeLayer =
            new ShapeLayerView(shapePath, currentFill, currentStroke, currentTrim, currentTransform,
                getCallback());
        addLayer(shapeLayer);
      } else if (item instanceof RectangleShape) {
        RectangleShape shapeRect = (RectangleShape) item;
        RectLayer shapeLayer =
            new RectLayer(shapeRect, currentFill, currentStroke, currentTrim, currentTransform,
                getCallback());
        addLayer(shapeLayer);
      } else if (item instanceof CircleShape) {
        CircleShape shapeCircle = (CircleShape) item;
        EllipseLayer shapeLayer =
            new EllipseLayer(shapeCircle, currentFill, currentStroke, currentTrim,
                currentTransform, getCallback());
        addLayer(shapeLayer);
      } else if (item instanceof PolystarShape) {
        PolystarShape polystarShape = (PolystarShape) item;
        PolystarLayer shapeLayer = new PolystarLayer(polystarShape, currentFill, currentStroke,
            currentTrim, currentTransform, getCallback());
        addLayer(shapeLayer);
      } else if (item instanceof ShapeGroup) {
        ShapeGroup shapeGroup = (ShapeGroup) item;
        GroupLayerView groupLayer =
            new GroupLayerView(shapeGroup, currentFill, currentStroke, currentTrim,
                currentTransform, getCallback());
        addLayer(groupLayer);
      }

    }
  }
}
