package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Matrix;

class StrokeContent extends BaseStrokeContent {

  private final KeyframeAnimation<Integer> colorAnimation;

  StrokeContent(final LottieDrawable lottieDrawable, BaseLayer layer, ShapeStroke stroke) {
    super(lottieDrawable, layer, stroke.getCapType().toPaintCap(),
        stroke.getJoinType().toPaintJoin(), stroke.getOpacity(), stroke.getWidth(),
        stroke.getLineDashPattern(), stroke.getDashOffset());
    colorAnimation = stroke.getColor().createAnimation();
    colorAnimation.addUpdateListener(this);
    layer.addAnimation(colorAnimation);
  }

  @Override public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    paint.setColor(colorAnimation.getValue());
    super.draw(canvas, parentMatrix, parentAlpha);
  }
}
