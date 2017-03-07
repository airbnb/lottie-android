package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.NonNull;

class ShapeLayer extends AnimatableLayer {
  private MaskKeyframeAnimation mask;

  private final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint clearPain = new Paint();

  private final Layer layerModel;
  private final LottieComposition composition;
  private final ContentGroup contentGroup;

  ShapeLayer(LottieDrawable lottieDrawable, Layer layerModel, LottieComposition composition) {
    super(lottieDrawable, layerModel);
    this.layerModel = layerModel;
    this.composition = composition;

    ShapeGroup shapeGroup = new ShapeGroup(layerModel.getName(), layerModel.getShapes());
    contentGroup = new ContentGroup(shapeGroup, null, null, null, null, lottieDrawable);
    maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

    if (layerModel.getMasks() != null && !layerModel.getMasks().isEmpty()) {
      setMask(new MaskKeyframeAnimation(layerModel.getMasks()));
    }
  }

  private void setMask(MaskKeyframeAnimation mask) {
    this.mask = mask;
    for (BaseKeyframeAnimation<?, Path> animation : mask.getMaskAnimations()) {
      addAnimation(animation);
      animation.addUpdateListener(pathChangedListener);
    }
  }

  boolean hasMasks() {
    return mask != null && !mask.getMaskAnimations().isEmpty();
  }

  @Override void drawLayer(@NonNull Canvas canvas) {
    canvas.save();
    applyTransformsForParentLayersAndSelf(canvas);

    contentGroup.draw(canvas);

    canvas.restore();
  }
}
