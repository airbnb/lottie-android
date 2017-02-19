package com.airbnb.lottie;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

class ShapeLayerView extends AnimatableLayer {
  @Nullable private ShapeLayer fillLayer;
  @Nullable private ShapeLayer strokeLayer;

  ShapeLayerView(ShapePath shape, @Nullable ShapeFill fill,
      @Nullable ShapeStroke stroke, @Nullable ShapeTrimPath trim,
      AnimatableTransform transformModel, Drawable.Callback callback) {
    super(callback);
    setTransform(transformModel.createAnimation());
    if (fill != null) {
      fillLayer = new ShapeLayer(getCallback());
      fillLayer.setPath(shape.getShapePath().createAnimation());
      fillLayer.setColor(fill.getColor().createAnimation());
      fillLayer.setShapeOpacity(fill.getOpacity().createAnimation());
      fillLayer.setTransformOpacity(transformModel.getOpacity().createAnimation());
      fillLayer.setScale(transformModel.getScale().createAnimation());
      if (trim != null) {
        fillLayer.setTrimPath(trim.getStart().createAnimation(), trim.getEnd().createAnimation(),
            trim.getOffset().createAnimation());
      }
      addLayer(fillLayer);
    }

    if (stroke != null) {
      strokeLayer = new ShapeLayer(getCallback());
      strokeLayer.setIsStroke();
      strokeLayer.setPath(shape.getShapePath().createAnimation());
      strokeLayer.setColor(stroke.getColor().createAnimation());
      strokeLayer.setShapeOpacity(stroke.getOpacity().createAnimation());
      strokeLayer.setTransformOpacity(transformModel.getOpacity().createAnimation());
      strokeLayer.setLineWidth(stroke.getWidth().createAnimation());
      if (!stroke.getLineDashPattern().isEmpty()) {
        List<BaseKeyframeAnimation<?, Float>> dashPatternAnimations =
            new ArrayList<>(stroke.getLineDashPattern().size());
        for (AnimatableFloatValue dashPattern : stroke.getLineDashPattern()) {
          dashPatternAnimations.add(dashPattern.createAnimation());
        }
        strokeLayer.setDashPattern(dashPatternAnimations, stroke.getDashOffset().createAnimation());
      }
      strokeLayer.setLineCapType(stroke.getCapType());
      strokeLayer.setLineJoinType(stroke.getJoinType());
      strokeLayer.setScale(transformModel.getScale().createAnimation());
      if (trim != null) {
        strokeLayer.setTrimPath(trim.getStart().createAnimation(), trim.getEnd().createAnimation(),
            trim.getOffset().createAnimation());
      }
      addLayer(strokeLayer);
    }
  }

  @Override public void setAlpha(int alpha) {
    super.setAlpha(alpha);
    if (fillLayer != null) {
      fillLayer.setAlpha(alpha);
    }
    if (strokeLayer != null) {
      strokeLayer.setAlpha(alpha);
    }
  }
}
