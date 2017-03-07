package com.airbnb.lottie;

import android.support.annotation.Nullable;

class ContentGroup extends AnimatableLayer {
  private final ShapeGroup shapeGroup;
  @Nullable private final AnimatableTransform transform;

  ContentGroup(ShapeGroup shapeGroup, @Nullable ShapeFill previousFill,
      @Nullable ShapeStroke previousStroke, @Nullable ShapeTrimPath previousTrimPath,
      @Nullable AnimatableTransform transform, LottieDrawable lottieDrawable) {
    super(lottieDrawable);
    this.shapeGroup = shapeGroup;
    this.transform = transform;
    setupShapeGroupWithFill(previousFill, previousStroke, previousTrimPath);
  }

  private void setupShapeGroupWithFill(ShapeFill previousFill,
      ShapeStroke previousStroke, ShapeTrimPath previousTrimPath) {
    if (transform != null) {
      setTransform(transform.createAnimation());
    }

    ShapeFill currentFill = previousFill;
    ShapeStroke currentStroke = previousStroke;
    AnimatableTransform currentTransform = null;
    ShapeTrimPath currentTrim = previousTrimPath;

    for (int i = shapeGroup.getItems().size() - 1; i >= 0; i--) {
      Object item = shapeGroup.getItems().get(i);
      if (item instanceof AnimatableTransform) {
        currentTransform = (AnimatableTransform) item;
      } else if (item instanceof ShapeStroke) {
        currentStroke = (ShapeStroke) item;
      } else if (item instanceof ShapeFill) {
        currentFill = (ShapeFill) item;
      } else if (item instanceof ShapeTrimPath) {
        currentTrim = (ShapeTrimPath) item;
      } else if (item instanceof ShapePath) {
        ShapePath shapePath = (ShapePath) item;
        ShapeContentFillAndStroke shapeLayer = new ShapeContentFillAndStroke(
            shapePath, currentFill, currentStroke, currentTrim, currentTransform, lottieDrawable);
        addLayer(shapeLayer);
      } else if (item instanceof RectangleShape) {
        RectangleShape shapeRect = (RectangleShape) item;
        RectContentFillAndStroke shapeLayer = new RectContentFillAndStroke(
            shapeRect, currentFill, currentStroke, currentTrim, currentTransform, lottieDrawable);
        addLayer(shapeLayer);
      } else if (item instanceof CircleShape) {
        CircleShape shapeCircle = (CircleShape) item;
        EllipseContentFillAndStroke shapeLayer = new EllipseContentFillAndStroke(
            shapeCircle, currentFill, currentStroke, currentTrim, currentTransform, lottieDrawable);
        addLayer(shapeLayer);
      } else if (item instanceof PolystarShape) {
        PolystarShape polystarShape = (PolystarShape) item;
        PolystarContentFillAndStroke
            shapeLayer = new PolystarContentFillAndStroke(polystarShape, currentFill, currentStroke,
            currentTrim, currentTransform, lottieDrawable);
        addLayer(shapeLayer);
      } else if (item instanceof ShapeGroup) {
        ShapeGroup shapeGroup = (ShapeGroup) item;
        ContentGroup groupLayer = new ContentGroup(
            shapeGroup, currentFill, currentStroke, currentTrim, currentTransform, lottieDrawable);
        addLayer(groupLayer);
      }

    }
  }
}
