package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.ArrayList;
import java.util.List;

class GradientFillContent implements DrawingContent, BaseKeyframeAnimation.AnimationListener {
  private final Path path = new Path();
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF boundsRect = new RectF();
  private final List<PathContent> paths = new ArrayList<>();
  private final KeyframeAnimation<GradientColor> colorAnimation;
  private final KeyframeAnimation<Integer> opacityAnimation;
  private final KeyframeAnimation<PointF> startPointAnimation;
  private final KeyframeAnimation<PointF> endPointAnimation;
  private final LottieDrawable lottieDrawable;

  GradientFillContent(final LottieDrawable lottieDrawable, BaseLayer layer, GradientFill fill) {
    this.lottieDrawable = lottieDrawable;
    path.setFillType(fill.getFillType());

    colorAnimation = fill.getGradientColor().createAnimation();
    colorAnimation.addUpdateListener(this);
    layer.addAnimation(colorAnimation);

    opacityAnimation = fill.getOpacity().createAnimation();
    opacityAnimation.addUpdateListener(this);
    layer.addAnimation(opacityAnimation);

    startPointAnimation = fill.getStartPoint().createAnimation();
    startPointAnimation.addUpdateListener(this);
    layer.addAnimation(startPointAnimation);

    endPointAnimation = fill.getEndPoint().createAnimation();
    endPointAnimation.addUpdateListener(this);
    layer.addAnimation(endPointAnimation);
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
    path.reset();
    for (int i = 0; i < paths.size(); i++) {
      path.addPath(paths.get(i).getPath(), parentMatrix);
    }

    path.computeBounds(boundsRect, false);

    paint.setShader(getShader());
    int alpha = (int) ((parentAlpha / 255f * opacityAnimation.getValue() / 100f) * 255);
    paint.setAlpha(alpha);

    canvas.drawPath(path, paint);
  }

  private LinearGradient getShader() {
    // TODO: cache these
    PointF startPoint = startPointAnimation.getValue();
    PointF endPoint = endPointAnimation.getValue();
    GradientColor gradientColor = colorAnimation.getValue();
    int[] colors = gradientColor.getColors();
    float[] positions = gradientColor.getPositions();
    int x0 = (int) (boundsRect.left + boundsRect.width() / 2 + startPoint.x);
    int y0 = (int) (boundsRect.top + boundsRect.height() / 2 + startPoint.y);
    int x1 = (int) (boundsRect.left + boundsRect.width() / 2 + endPoint.x);
    int y1 = (int) (boundsRect.top + boundsRect.height() / 2 + endPoint.y);
    return new LinearGradient(x0 , y0, x1, y1, colors, positions, Shader.TileMode.CLAMP);
  }
}
