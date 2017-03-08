package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

class FillContent implements Content, DrawingContent {
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final List<PathContent> paths = new ArrayList<>();
  private final KeyframeAnimation<Integer> colorAnimation;
  private final KeyframeAnimation<Integer> opacityAnimation;

  FillContent(final LottieDrawable lottieDrawable, AnimatableLayer layer, ShapeFill fill) {
    if (fill.getColor() == null || fill.getOpacity() == null ) {
      colorAnimation = null;
      opacityAnimation = null;
      return;
    }

    BaseKeyframeAnimation.AnimationListener<Integer> listener =
        new BaseKeyframeAnimation.AnimationListener<Integer>() {
          @Override public void onValueChanged(Integer value) {
            lottieDrawable.invalidateSelf();
          }
        };

    colorAnimation = fill.getColor().createAnimation();
    colorAnimation.addUpdateListener(listener);
    layer.addAnimation(colorAnimation);
    opacityAnimation = fill.getOpacity().createAnimation();
    opacityAnimation.addUpdateListener(listener);
    layer.addAnimation(opacityAnimation);
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    for (int i = 0; i < contentsAfter.size(); i++) {
      Content content = contentsAfter.get(i);
      if (content instanceof PathContent) {
        paths.add((PathContent) content);
      }
    }
  }

  @Override public void draw(Canvas canvas, Matrix transformMatrix, int alpha) {
    canvas.save();
    canvas.setMatrix(transformMatrix);
    paint.setColor(colorAnimation.getValue());
    paint.setAlpha(opacityAnimation.getValue() * 255 / 100);

    for (int i = 0; i < paths.size(); i++) {
      canvas.drawPath(paths.get(0).getPath(), paint);
    }
  }
}
