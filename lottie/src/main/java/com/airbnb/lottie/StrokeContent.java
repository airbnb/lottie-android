package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StrokeContent implements Content, DrawingContent {

  private final Path path = new Path();
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final List<PathContent> paths = new ArrayList<>();

  private final BaseKeyframeAnimation<?, Integer> colorAnimation;
  private final BaseKeyframeAnimation<?, Float> widthAnimation;
  private final BaseKeyframeAnimation<?, Integer> opacityAnimation;
  private final List<BaseKeyframeAnimation<?, Float>> dashPatternAnimations;
  @Nullable private final BaseKeyframeAnimation<?, Float> offsetAnimation;

  StrokeContent(final LottieDrawable lottieDrawable, AnimatableLayer layer, ShapeStroke stroke) {
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeCap(stroke.getCapType().toPaintCap());
    paint.setStrokeJoin(stroke.getJoinType().toPaintJoin());

    colorAnimation = stroke.getColor().createAnimation();
    opacityAnimation = stroke.getOpacity().createAnimation();
    widthAnimation = stroke.getWidth().createAnimation();

    if (stroke.getDashOffset() == null) {
      offsetAnimation = null;
    } else {
      offsetAnimation = stroke.getDashOffset().createAnimation();
    }
    List<AnimatableFloatValue> dashPattern = stroke.getLineDashPattern();
    dashPatternAnimations = new ArrayList<>(dashPattern.size());
    for (int i = 0; i < dashPattern.size(); i++) {
      dashPatternAnimations.add(dashPattern.get(i).createAnimation());
    }

    layer.addAnimation(colorAnimation);
    layer.addAnimation(opacityAnimation);
    layer.addAnimation(widthAnimation);
    for (int i = 0; i < dashPatternAnimations.size(); i++) {
      layer.addAnimation(dashPatternAnimations.get(i));
    }
    if (offsetAnimation != null) {
      layer.addAnimation(offsetAnimation);
    }

    BaseKeyframeAnimation.AnimationListener<Float> floatListener =
        new BaseKeyframeAnimation.AnimationListener<Float>() {
          @Override public void onValueChanged(Float value) {
            lottieDrawable.invalidateSelf();
          }
        };
    BaseKeyframeAnimation.AnimationListener<Integer> integerListener =
        new BaseKeyframeAnimation.AnimationListener<Integer>() {
          @Override public void onValueChanged(Integer value) {
            lottieDrawable.invalidateSelf();
          }
        };
    colorAnimation.addUpdateListener(integerListener);
    opacityAnimation.addUpdateListener(integerListener);
    widthAnimation.addUpdateListener(floatListener);

    for (int i = 0; i < dashPattern.size(); i++) {
      dashPatternAnimations.get(i).addUpdateListener(floatListener);
    }
    if (offsetAnimation != null) {
      offsetAnimation.addUpdateListener(floatListener);
    }
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    for (int i = 0; i < contentsAfter.size(); i++) {
      Content content = contentsAfter.get(i);
      if (content instanceof PathContent) {
        paths.add((PathContent) content);
      }
    }
  }

  @Override public void draw(Canvas canvas, Matrix parentMatrix, int alpha) {
    paint.setColor(colorAnimation.getValue());
    paint.setAlpha(opacityAnimation.getValue() * 255 / 100);
    paint.setStrokeWidth(widthAnimation.getValue());

    path.reset();
    for (int i = 0; i < paths.size(); i++) {
      path.addPath(paths.get(i).getPath(), parentMatrix);
    }

    canvas.drawPath(path, paint);
  }
}
