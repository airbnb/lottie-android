package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

class StrokeContent implements DrawingContent, BaseKeyframeAnimation.AnimationListener {
  private final Path path = new Path();
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final LottieDrawable lottieDrawable;
  private final List<PathContent> paths = new ArrayList<>();
  private final float[] dashPatternValues;

  private final BaseKeyframeAnimation<?, Integer> colorAnimation;
  private final BaseKeyframeAnimation<?, Float> widthAnimation;
  private final BaseKeyframeAnimation<?, Integer> opacityAnimation;
  private final List<BaseKeyframeAnimation<?, Float>> dashPatternAnimations;
  @Nullable private final BaseKeyframeAnimation<?, Float> dashPatternOffsetAnimation;

  StrokeContent(final LottieDrawable lottieDrawable, BaseLayer layer, ShapeStroke stroke) {
    this.lottieDrawable = lottieDrawable;
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeCap(stroke.getCapType().toPaintCap());
    paint.setStrokeJoin(stroke.getJoinType().toPaintJoin());

    colorAnimation = stroke.getColor().createAnimation();
    opacityAnimation = stroke.getOpacity().createAnimation();
    widthAnimation = stroke.getWidth().createAnimation();

    if (stroke.getDashOffset() == null) {
      dashPatternOffsetAnimation = null;
    } else {
      dashPatternOffsetAnimation = stroke.getDashOffset().createAnimation();
    }
    List<AnimatableFloatValue> dashPattern = stroke.getLineDashPattern();
    dashPatternAnimations = new ArrayList<>(dashPattern.size());
    dashPatternValues = new float[dashPattern.size()];

    for (int i = 0; i < dashPattern.size(); i++) {
      dashPatternAnimations.add(dashPattern.get(i).createAnimation());
    }

    layer.addAnimation(colorAnimation);
    layer.addAnimation(opacityAnimation);
    layer.addAnimation(widthAnimation);
    for (int i = 0; i < dashPatternAnimations.size(); i++) {
      layer.addAnimation(dashPatternAnimations.get(i));
    }
    if (dashPatternOffsetAnimation != null) {
      layer.addAnimation(dashPatternOffsetAnimation);
    }

    colorAnimation.addUpdateListener(this);
    opacityAnimation.addUpdateListener(this);
    widthAnimation.addUpdateListener(this);

    for (int i = 0; i < dashPattern.size(); i++) {
      dashPatternAnimations.get(i).addUpdateListener(this);
    }
    if (dashPatternOffsetAnimation != null) {
      dashPatternOffsetAnimation.addUpdateListener(this);
    }
  }

  @Override public void onValueChanged() {
    lottieDrawable.invalidateSelf();
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    for (int i = 0; i < contentsAfter.size(); i++) {
      Content content = contentsAfter.get(i);
      if (content instanceof PathContent) {
        paths.add((PathContent) content);
      }
    }
  }

  @Override public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    paint.setColor(colorAnimation.getValue());
    int alpha = (int) ((parentAlpha / 255f * opacityAnimation.getValue() / 100f) * 255);
    paint.setAlpha(alpha);
    paint.setStrokeWidth(widthAnimation.getValue() * Utils.getScale(parentMatrix));
    if (paint.getStrokeWidth() < 1) {
      // Android draws a hairline stroke for 0, After Effects doesn't.
      return;
    }
    applyDashPatternIfNeeded();

    path.reset();
    for (int i = 0; i < paths.size(); i++) {
      this.path.addPath(paths.get(i).getPath(), parentMatrix);
    }

    canvas.drawPath(path, paint);
  }

  private void applyDashPatternIfNeeded() {
    if (dashPatternAnimations.isEmpty()) {
      return;
    }

    float scale = lottieDrawable.getScale();
    for (int i = 0; i < dashPatternAnimations.size(); i++) {
      dashPatternValues[i] = dashPatternAnimations.get(i).getValue();
      // If the value of the dash pattern or gap is too small, the number of individual sections
      // approaches infinity as the value approaches 0.
      // To mitigate this, we essentially put a minimum value on the dash pattern size of 1px
      // and a minimum gap size of 0.01.
      if (i % 2 == 0) {
        if (dashPatternValues[i] < 1f) {
          dashPatternValues[i] = 1f;
        }
      } else {
        if (dashPatternValues[i] < 0.1f) {
          dashPatternValues[i] = 0.1f;
        }
      }
      dashPatternValues[i] *= scale;
    }
    float offset = dashPatternOffsetAnimation == null ? 0f : dashPatternOffsetAnimation.getValue();
    paint.setPathEffect(new DashPathEffect(dashPatternValues, offset));
  }
}
