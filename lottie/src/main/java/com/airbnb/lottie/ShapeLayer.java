package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import java.util.Collections;

class ShapeLayer extends BaseLayer {
  private final Matrix matrix = new Matrix();

  private final ContentGroup contentGroup;

  ShapeLayer(LottieDrawable lottieDrawable, Layer layerModel) {
    super(lottieDrawable, layerModel);

    ShapeGroup shapeGroup = new ShapeGroup(layerModel.getName(), layerModel.getShapes());
    contentGroup = new ContentGroup(lottieDrawable, this, shapeGroup);
    contentGroup.setContents(Collections.<Content>emptyList(), Collections.<Content>emptyList());
  }

  @Override void drawLayer(@NonNull Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    contentGroup.draw(canvas, parentMatrix, parentAlpha);
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix) {
    super.getBounds(outBounds, parentMatrix);
    contentGroup.getBounds(outBounds, boundsMatrix);
  }
}
