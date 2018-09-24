package com.airbnb.lottie.animation.content;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.content.ShapeTrimPath;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.LottieValueCallback;

import java.util.ArrayList;
import java.util.List;

import static com.airbnb.lottie.utils.MiscUtils.clamp;

public abstract class BaseStrokeContent
    implements BaseKeyframeAnimation.AnimationListener, KeyPathElementContent, DrawingContent {

  private final PathMeasure pm = new PathMeasure();
  private final Path path = new Path();
  private final Path trimPathPath = new Path();
  private final RectF rect = new RectF();
  private final LottieDrawable lottieDrawable;
  private final BaseLayer layer;
  private final List<PathGroup> pathGroups = new ArrayList<>();
  private final float[] dashPatternValues;
  final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

  private final BaseKeyframeAnimation<?, Float> widthAnimation;
  private final BaseKeyframeAnimation<?, Integer> opacityAnimation;
  private final List<BaseKeyframeAnimation<?, Float>> dashPatternAnimations;
  @Nullable private final BaseKeyframeAnimation<?, Float> dashPatternOffsetAnimation;
  @Nullable private BaseKeyframeAnimation<ColorFilter, ColorFilter> colorFilterAnimation;

  BaseStrokeContent(final LottieDrawable lottieDrawable, BaseLayer layer, Paint.Cap cap,
      Paint.Join join, float miterLimit, AnimatableIntegerValue opacity, AnimatableFloatValue width,
      List<AnimatableFloatValue> dashPattern, AnimatableFloatValue offset) {
    this.lottieDrawable = lottieDrawable;
    this.layer = layer;

    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeCap(cap);
    paint.setStrokeJoin(join);
    paint.setStrokeMiter(miterLimit);

    opacityAnimation = opacity.createAnimation();
    widthAnimation = width.createAnimation();

    if (offset == null) {
      dashPatternOffsetAnimation = null;
    } else {
      dashPatternOffsetAnimation = offset.createAnimation();
    }
    dashPatternAnimations = new ArrayList<>(dashPattern.size());
    dashPatternValues = new float[dashPattern.size()];

    for (int i = 0; i < dashPattern.size(); i++) {
      dashPatternAnimations.add(dashPattern.get(i).createAnimation());
    }

    layer.addAnimation(opacityAnimation);
    layer.addAnimation(widthAnimation);
    for (int i = 0; i < dashPatternAnimations.size(); i++) {
      layer.addAnimation(dashPatternAnimations.get(i));
    }
    if (dashPatternOffsetAnimation != null) {
      layer.addAnimation(dashPatternOffsetAnimation);
    }

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
    TrimPathContent trimPathContentBefore = null;
    for (int i = contentsBefore.size() - 1; i >= 0; i--) {
      Content content = contentsBefore.get(i);
      if (content instanceof TrimPathContent &&
          ((TrimPathContent) content).getType() == ShapeTrimPath.Type.Individually) {
        trimPathContentBefore = (TrimPathContent) content;
      }
    }
    if (trimPathContentBefore != null) {
      trimPathContentBefore.addListener(this);
    }

    PathGroup currentPathGroup = null;
    for (int i = contentsAfter.size() - 1; i >= 0; i--) {
      Content content = contentsAfter.get(i);
      if (content instanceof TrimPathContent &&
          ((TrimPathContent) content).getType() == ShapeTrimPath.Type.Individually) {
        if (currentPathGroup != null) {
          pathGroups.add(currentPathGroup);
        }
        currentPathGroup = new PathGroup((TrimPathContent) content);
        ((TrimPathContent) content).addListener(this);
      } else if (content instanceof PathContent) {
        if (currentPathGroup == null) {
          currentPathGroup = new PathGroup(trimPathContentBefore);
        }
        currentPathGroup.paths.add((PathContent) content);
      }
    }
    if (currentPathGroup != null) {
      pathGroups.add(currentPathGroup);
    }
  }

  @Override public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    L.beginSection("StrokeContent#draw");
    int alpha = (int) ((parentAlpha / 255f * opacityAnimation.getValue() / 100f) * 255);
    paint.setAlpha(clamp(alpha, 0, 255));
    paint.setStrokeWidth(widthAnimation.getValue() * Utils.getScale(parentMatrix));
    if (paint.getStrokeWidth() <= 0) {
      // Android draws a hairline stroke for 0, After Effects doesn't.
      L.endSection("StrokeContent#draw");
      return;
    }
    applyDashPatternIfNeeded(parentMatrix);

    if (colorFilterAnimation != null) {
      paint.setColorFilter(colorFilterAnimation.getValue());
    }

    for (int i = 0; i < pathGroups.size(); i++) {
      PathGroup pathGroup = pathGroups.get(i);


      if (pathGroup.trimPath != null) {
        applyTrimPath(canvas, pathGroup, parentMatrix);
      } else {
        L.beginSection("StrokeContent#buildPath");
        path.reset();
        for (int j = pathGroup.paths.size() - 1; j >= 0; j--) {
          path.addPath(pathGroup.paths.get(j).getPath(), parentMatrix);
        }
        L.endSection("StrokeContent#buildPath");
        L.beginSection("StrokeContent#drawPath");
        canvas.drawPath(path, paint);
        L.endSection("StrokeContent#drawPath");
      }
    }
    L.endSection("StrokeContent#draw");
  }

  private void applyTrimPath(Canvas canvas, PathGroup pathGroup, Matrix parentMatrix) {
    L.beginSection("StrokeContent#applyTrimPath");
    if (pathGroup.trimPath == null) {
      L.endSection("StrokeContent#applyTrimPath");
      return;
    }
    path.reset();
    for (int j = pathGroup.paths.size() - 1; j >= 0; j--) {
      path.addPath(pathGroup.paths.get(j).getPath(), parentMatrix);
    }
    pm.setPath(path, false);
    float totalLength = pm.getLength();
    while (pm.nextContour()) {
      totalLength += pm.getLength();
    }
    float offsetLength = totalLength * pathGroup.trimPath.getOffset().getValue() / 360f;
    float startLength =
        totalLength * pathGroup.trimPath.getStart().getValue() / 100f + offsetLength;
    float endLength =
        totalLength * pathGroup.trimPath.getEnd().getValue() / 100f + offsetLength;

    float currentLength = 0;
    for (int j = pathGroup.paths.size() - 1; j >= 0; j--) {
      trimPathPath.set(pathGroup.paths.get(j).getPath());
      trimPathPath.transform(parentMatrix);
      pm.setPath(trimPathPath, false);
      float length = pm.getLength();
      if (endLength > totalLength && endLength - totalLength < currentLength + length &&
          currentLength < endLength - totalLength) {
        // Draw the segment when the end is greater than the length which wraps around to the
        // beginning.
        float startValue;
        if (startLength > totalLength) {
          startValue = (startLength - totalLength) / length;
        } else {
          startValue = 0;
        }
        float endValue = Math.min((endLength - totalLength) / length, 1);
        Utils.applyTrimPathIfNeeded(trimPathPath, startValue, endValue, 0);
        canvas.drawPath(trimPathPath, paint);
      } else
        //noinspection StatementWithEmptyBody
        if (currentLength + length < startLength || currentLength > endLength) {
          // Do nothing
        } else if (currentLength + length <= endLength && startLength < currentLength) {
          canvas.drawPath(trimPathPath, paint);
        } else {
          float startValue;
          if (startLength < currentLength) {
            startValue = 0;
          } else {
            startValue = (startLength - currentLength) / length;
          }
          float endValue;
          if (endLength > currentLength + length) {
            endValue = 1f;
          } else {
            endValue = (endLength - currentLength) / length;
          }
          Utils.applyTrimPathIfNeeded(trimPathPath, startValue, endValue, 0);
          canvas.drawPath(trimPathPath, paint);
        }
      currentLength += length;
    }
    L.endSection("StrokeContent#applyTrimPath");
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix) {
    L.beginSection("StrokeContent#getBounds");
    path.reset();
    for (int i = 0; i < pathGroups.size(); i++) {
      PathGroup pathGroup = pathGroups.get(i);
      for (int j = 0; j < pathGroup.paths.size(); j++) {
        path.addPath(pathGroup.paths.get(j).getPath(), parentMatrix);
      }
    }
    path.computeBounds(rect, false);

    float width = widthAnimation.getValue();
    rect.set(rect.left - width / 2f, rect.top - width / 2f,
        rect.right + width / 2f, rect.bottom + width / 2f);
    outBounds.set(rect);
    // Add padding to account for rounding errors.
    outBounds.set(
        outBounds.left - 1,
        outBounds.top - 1,
        outBounds.right + 1,
        outBounds.bottom + 1
    );
    L.endSection("StrokeContent#getBounds");
  }

  private void applyDashPatternIfNeeded(Matrix parentMatrix) {
    L.beginSection("StrokeContent#applyDashPattern");
    if (dashPatternAnimations.isEmpty()) {
      L.endSection("StrokeContent#applyDashPattern");
      return;
    }

    float scale = Utils.getScale(parentMatrix);
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
    L.endSection("StrokeContent#applyDashPattern");
  }

  @Override public void resolveKeyPath(
      KeyPath keyPath, int depth, List<KeyPath> accumulator, KeyPath currentPartialKeyPath) {
    MiscUtils.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath, this);
  }

  @SuppressWarnings("unchecked")
  @Override
  @CallSuper
  public <T> void addValueCallback(T property, @Nullable LottieValueCallback<T> callback) {
    if (property == LottieProperty.OPACITY) {
      opacityAnimation.setValueCallback((LottieValueCallback<Integer>) callback);
    } else if (property == LottieProperty.STROKE_WIDTH) {
      widthAnimation.setValueCallback((LottieValueCallback<Float>) callback);
    } else if (property == LottieProperty.COLOR_FILTER) {
      if (callback == null) {
        colorFilterAnimation = null;
      } else {
        colorFilterAnimation =
            new ValueCallbackKeyframeAnimation<>((LottieValueCallback<ColorFilter>) callback);
        colorFilterAnimation.addUpdateListener(this);
        layer.addAnimation(colorFilterAnimation);
      }
    }
  }

  /**
   * Data class to help drawing trim paths individually.
   */
  private static final class PathGroup {
    private final List<PathContent> paths = new ArrayList<>();
    @Nullable private final TrimPathContent trimPath;

    private PathGroup(@Nullable TrimPathContent trimPath) {
      this.trimPath = trimPath;
    }
  }
}
