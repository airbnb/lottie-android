package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;

import java.util.ArrayList;
import java.util.List;

class GradientFillContent implements DrawingContent, BaseKeyframeAnimation.AnimationListener {
  /**
   * Cache the gradients such that it runs at 30fps.
   */
  private static final int CACHE_STEPS_MS = 32;
  private final String name;
  private final LongSparseArray<LinearGradient> linearGradientCache = new LongSparseArray<>();
  private final LongSparseArray<RadialGradient> radialGradientCache = new LongSparseArray<>();
  private final Matrix shaderMatrix = new Matrix();
  private final Path path = new Path();
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF boundsRect = new RectF();
  private final List<PathContent> paths = new ArrayList<>();
  private final GradientType type;
  private final KeyframeAnimation<GradientColor> colorAnimation;
  private final KeyframeAnimation<Integer> opacityAnimation;
  private final KeyframeAnimation<PointF> startPointAnimation;
  private final KeyframeAnimation<PointF> endPointAnimation;
  private final LottieDrawable lottieDrawable;
  private final int cacheSteps;

  GradientFillContent(final LottieDrawable lottieDrawable, BaseLayer layer, GradientFill fill) {
    name = fill.getName();
    this.lottieDrawable = lottieDrawable;
    type = fill.getGradientType();
    path.setFillType(fill.getFillType());
    cacheSteps = (int) (lottieDrawable.getComposition().getDuration() / CACHE_STEPS_MS);

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

    Shader shader;
    if (type == GradientType.Linear) {
      shader = getLinearGradient();
    } else {
      shader = getRadialGradient();
    }
    shaderMatrix.set(parentMatrix);
    shader.setLocalMatrix(shaderMatrix);
    paint.setShader(shader);

    int alpha = (int) ((parentAlpha / 255f * opacityAnimation.getValue() / 100f) * 255);
    paint.setAlpha(alpha);

    canvas.drawPath(path, paint);
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix) {
    path.reset();
    for (int i = 0; i < paths.size(); i++) {
      path.addPath(paths.get(i).getPath(), parentMatrix);
    }

    path.computeBounds(outBounds, false);
    // Add padding to account for rounding errors.
    outBounds.set(
        outBounds.left - 1,
        outBounds.top - 1,
        outBounds.right + 1,
        outBounds.bottom + 1
    );
  }

  @Override public void addColorFilter(@Nullable String layerName, @Nullable String contentName,
      @Nullable ColorFilter colorFilter) {
    // Do nothing
  }

  @Override public String getName() {
    return name;
  }

  private LinearGradient getLinearGradient() {
    int gradientHash = getGradientHash();
    LinearGradient gradient = linearGradientCache.get(gradientHash);
    if (gradient != null) {
      return gradient;
    }
    PointF startPoint = startPointAnimation.getValue();
    PointF endPoint = endPointAnimation.getValue();
    GradientColor gradientColor = colorAnimation.getValue();
    int[] colors = gradientColor.getColors();
    float[] positions = gradientColor.getPositions();
    gradient = new LinearGradient(startPoint.x, startPoint.y, endPoint.x, endPoint.y, colors,
        positions, Shader.TileMode.CLAMP);
    linearGradientCache.put(gradientHash, gradient);
    return gradient;
  }

  private RadialGradient getRadialGradient() {
    int gradientHash = getGradientHash();
    RadialGradient gradient = radialGradientCache.get(gradientHash);
    if (gradient != null) {
      return gradient;
    }
    PointF startPoint = startPointAnimation.getValue();
    PointF endPoint = endPointAnimation.getValue();
    GradientColor gradientColor = colorAnimation.getValue();
    int[] colors = gradientColor.getColors();
    float[] positions = gradientColor.getPositions();
    float x0 = startPoint.x;
    float y0 = startPoint.y;
    float x1 = endPoint.x;
    float y1 = endPoint.y;
    float r = (float) Math.hypot(x1 - x0, y1 - y0);
    gradient = new RadialGradient(x0, y0, r, colors, positions, Shader.TileMode.CLAMP);
    radialGradientCache.put(gradientHash, gradient);
    return gradient;
  }

  private int getGradientHash() {
    int startPointProgress = Math.round(startPointAnimation.getProgress() * cacheSteps);
    int endPointProgress = Math.round(endPointAnimation.getProgress() * cacheSteps);
    int colorProgress = Math.round(colorAnimation.getProgress() * cacheSteps);
    int hash = 17;
    hash = hash * 31 * startPointProgress;
    hash = hash * 31 * endPointProgress;
    hash = hash * 31 * colorProgress;
    return hash;
  }
}
