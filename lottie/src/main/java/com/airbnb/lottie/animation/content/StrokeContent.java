package com.airbnb.lottie.animation.content;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.model.content.ShapeStroke;
import com.airbnb.lottie.model.layer.BaseLayer;

public class StrokeContent extends BaseStrokeContent implements ColorFilterableContent {

  private final String name;
  private final BaseKeyframeAnimation<Integer, Integer> colorAnimation;

  public StrokeContent(final LottieDrawable lottieDrawable, BaseLayer layer, ShapeStroke stroke) {
    super(lottieDrawable, layer, stroke.getCapType().toPaintCap(),
        stroke.getJoinType().toPaintJoin(), stroke.getOpacity(), stroke.getWidth(),
        stroke.getLineDashPattern(), stroke.getDashOffset());
    name = stroke.getName();
    colorAnimation = stroke.getColor().createAnimation();
    colorAnimation.addUpdateListener(this);
    layer.addAnimation(colorAnimation);
  }

  @Override
  public void setColorFilter(ColorFilter colorFilter) {
    paint.setColorFilter(colorFilter);
  }

  @Override public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    paint.setColor(colorAnimation.getValue());
    super.draw(canvas, parentMatrix, parentAlpha);
  }

  @Override public String getName() {
    return name;
  }

  public BaseKeyframeAnimation<Integer, Integer> getColor() {
    return colorAnimation;
  }
}
