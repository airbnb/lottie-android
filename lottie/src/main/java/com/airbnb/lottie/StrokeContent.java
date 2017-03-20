package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.support.annotation.Nullable;

class StrokeContent extends BaseStrokeContent {

  private final String name;
  private final KeyframeAnimation<Integer> colorAnimation;

  StrokeContent(final LottieDrawable lottieDrawable, BaseLayer layer, ShapeStroke stroke) {
    super(lottieDrawable, layer, stroke.getCapType().toPaintCap(),
        stroke.getJoinType().toPaintJoin(), stroke.getOpacity(), stroke.getWidth(),
        stroke.getLineDashPattern(), stroke.getDashOffset());
    name = stroke.getName();
    colorAnimation = stroke.getColor().createAnimation();
    colorAnimation.addUpdateListener(this);
    layer.addAnimation(colorAnimation);
  }

  @Override
  public void addColorFilter(@Nullable String layerName, @Nullable String contentName,
      @Nullable ColorFilter colorFilter) {
    paint.setColorFilter(colorFilter);
  }

  @Override public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    paint.setColor(colorAnimation.getValue());
    super.draw(canvas, parentMatrix, parentAlpha);
  }

  @Override public String getName() {
    return name;
  }
}
