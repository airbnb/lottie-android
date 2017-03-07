package com.airbnb.lottie;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

class ShapeContentFillAndStroke extends AnimatableLayer {
  @Nullable private ShapeContent fillLayer;
  @Nullable private ShapeContent strokeLayer;

  ShapeContentFillAndStroke(ShapePath shape, @Nullable ShapeFill fill,
      @Nullable ShapeStroke stroke, @Nullable ShapeTrimPath trim,
      AnimatableTransform transformModel, LottieDrawable lottieDrawable) {
    super(lottieDrawable);
    setTransform(transformModel.createAnimation());
    if (fill != null) {
      fillLayer = new ShapeContent(lottieDrawable);
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
      strokeLayer = new ShapeContent(lottieDrawable);
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

  public void setAlpha(int alpha) {
    if (fillLayer != null) {
      fillLayer.setAlpha(alpha);
    }
    if (strokeLayer != null) {
      strokeLayer.setAlpha(alpha);
    }
  }
}
