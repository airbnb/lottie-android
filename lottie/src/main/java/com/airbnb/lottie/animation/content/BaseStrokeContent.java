package com.airbnb.lottie.animation.content;

import static com.airbnb.lottie.utils.MiscUtils.clamp;

import android.graphics.BlurMaskFilter;
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
import com.airbnb.lottie.animation.LPaint;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.DropShadowKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.FloatKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.IntegerKeyframeAnimation;
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

public abstract class BaseStrokeContent
    implements BaseKeyframeAnimation.AnimationListener, KeyPathElementContent, DrawingContent {

  private final PathMeasure pm = new PathMeasure();
  private final Path path = new Path();
  private final Path trimPathPath = new Path();
  private final RectF rect = new RectF();
  private final LottieDrawable lottieDrawable;
  protected final BaseLayer layer;
  private final List<PathGroup> pathGroups = new ArrayList<>();
  private final float[] dashPatternValues;
  final Paint paint = new LPaint(Paint.ANTI_ALIAS_FLAG);


  private final BaseKeyframeAnimation<?, Float> widthAnimation;
  private final BaseKeyframeAnimation<?, Integer> opacityAnimation;
  private final List<BaseKeyframeAnimation<?, Float>> dashPatternAnimations;
  @Nullable private final BaseKeyframeAnimation<?, Float> dashPatternOffsetAnimation;
  @Nullable private BaseKeyframeAnimation<ColorFilter, ColorFilter> colorFilterAnimation;
  @Nullable private BaseKeyframeAnimation<Float, Float> blurAnimation;
  float blurMaskFilterRadius = 0f;

  @Nullable private DropShadowKeyframeAnimation dropShadowAnimation;

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

    if (layer.getBlurEffect() != null) {
      blurAnimation = layer.getBlurEffect().getBlurriness().createAnimation();
      blurAnimation.addUpdateListener(this);
      layer.addAnimation(blurAnimation);
    }
    if (layer.getDropShadowEffect() != null) {
      dropShadowAnimation = new DropShadowKeyframeAnimation(this, layer, layer.getDropShadowEffect());
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
          ((TrimPathContent) content).getType() == ShapeTrimPath.Type.INDIVIDUALLY) {
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
          ((TrimPathContent) content).getType() == ShapeTrimPath.Type.INDIVIDUALLY) {
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
    if (L.isTraceEnabled()) {
      L.beginSection("StrokeContent#draw");
    }
    if (Utils.hasZeroScaleAxis(parentMatrix)) {
      if (L.isTraceEnabled()) {
        L.endSection("StrokeContent#draw");
      }
      return;
    }
    int alpha = (int) ((parentAlpha / 255f * ((IntegerKeyframeAnimation) opacityAnimation).getIntValue() / 100f) * 255);
    paint.setAlpha(clamp(alpha, 0, 255));
    paint.setStrokeWidth(((FloatKeyframeAnimation) widthAnimation).getFloatValue());
    if (paint.getStrokeWidth() <= 0) {
      // Android draws a hairline stroke for 0, After Effects doesn't.
      if (L.isTraceEnabled()) {
        L.endSection("StrokeContent#draw");
      }
      return;
    }
    applyDashPatternIfNeeded();

    if (colorFilterAnimation != null) {
      paint.setColorFilter(colorFilterAnimation.getValue());
    }

    if (blurAnimation != null) {
      float blurRadius = blurAnimation.getValue();
      if (blurRadius == 0f) {
        paint.setMaskFilter(null);
      } else if (blurRadius != blurMaskFilterRadius){
        BlurMaskFilter blur = layer.getBlurMaskFilter(blurRadius);
        paint.setMaskFilter(blur);
      }
      blurMaskFilterRadius = blurRadius;
    }
    if (dropShadowAnimation != null) {
      dropShadowAnimation.applyTo(paint);
    }

    canvas.save();
    canvas.concat(parentMatrix);
    for (int i = 0; i < pathGroups.size(); i++) {
      PathGroup pathGroup = pathGroups.get(i);


      if (pathGroup.trimPath != null) {
        applyTrimPath(canvas, pathGroup);
      } else {
        if (L.isTraceEnabled()) {
          L.beginSection("StrokeContent#buildPath");
        }
        path.reset();
        for (int j = pathGroup.paths.size() - 1; j >= 0; j--) {
          path.addPath(pathGroup.paths.get(j).getPath());
        }
        if (L.isTraceEnabled()) {
          L.endSection("StrokeContent#buildPath");
          L.beginSection("StrokeContent#drawPath");
        }
        canvas.drawPath(path, paint);
        if (L.isTraceEnabled()) {
          L.endSection("StrokeContent#drawPath");
        }
      }
    }
    canvas.restore();
    if (L.isTraceEnabled()) {
      L.endSection("StrokeContent#draw");
    }
  }

  private void applyTrimPath(Canvas canvas, PathGroup pathGroup) {
    if (L.isTraceEnabled()) {
      L.beginSection("StrokeContent#applyTrimPath");
    }
    if (pathGroup.trimPath == null) {
      if (L.isTraceEnabled()) {
        L.endSection("StrokeContent#applyTrimPath");
      }
      return;
    }
    path.reset();
    for (int j = pathGroup.paths.size() - 1; j >= 0; j--) {
      path.addPath(pathGroup.paths.get(j).getPath());
    }
    float animStartValue = pathGroup.trimPath.getStart().getValue() / 100f;
    float animEndValue = pathGroup.trimPath.getEnd().getValue() / 100f;
    float animOffsetValue = pathGroup.trimPath.getOffset().getValue() / 360f;

    // If the start-end is ~100, consider it to be the full path.
    if (animStartValue < 0.01f && animEndValue > 0.99f) {
      canvas.drawPath(path, paint);
      if (L.isTraceEnabled()) {
        L.endSection("StrokeContent#applyTrimPath");
      }
      return;
    }

    pm.setPath(path, false);
    float totalLength = pm.getLength();
    while (pm.nextContour()) {
      totalLength += pm.getLength();
    }
    float offsetLength = totalLength * animOffsetValue;
    float startLength = totalLength * animStartValue + offsetLength;
    float endLength = Math.min(totalLength * animEndValue + offsetLength, startLength + totalLength - 1f);

    float currentLength = 0;
    for (int j = pathGroup.paths.size() - 1; j >= 0; j--) {
      trimPathPath.set(pathGroup.paths.get(j).getPath());
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
    if (L.isTraceEnabled()) {
      L.endSection("StrokeContent#applyTrimPath");
    }
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix, boolean applyParents) {
    if (L.isTraceEnabled()) {
      L.beginSection("StrokeContent#getBounds");
    }
    path.reset();
    for (int i = 0; i < pathGroups.size(); i++) {
      PathGroup pathGroup = pathGroups.get(i);
      for (int j = 0; j < pathGroup.paths.size(); j++) {
        path.addPath(pathGroup.paths.get(j).getPath(), parentMatrix);
      }
    }
    path.computeBounds(rect, false);

    float width = ((FloatKeyframeAnimation) widthAnimation).getFloatValue();
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
    if (L.isTraceEnabled()) {
      L.endSection("StrokeContent#getBounds");
    }
  }

  private void applyDashPatternIfNeeded() {
    if (L.isTraceEnabled()) {
      L.beginSection("StrokeContent#applyDashPattern");
    }
    if (dashPatternAnimations.isEmpty()) {
      if (L.isTraceEnabled()) {
        L.endSection("StrokeContent#applyDashPattern");
      }
      return;
    }

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
    }
    float offset = dashPatternOffsetAnimation == null ? 0f : dashPatternOffsetAnimation.getValue();
    paint.setPathEffect(new DashPathEffect(dashPatternValues, offset));
    if (L.isTraceEnabled()) {
      L.endSection("StrokeContent#applyDashPattern");
    }
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
      if (colorFilterAnimation != null) {
        layer.removeAnimation(colorFilterAnimation);
      }

      if (callback == null) {
        colorFilterAnimation = null;
      } else {
        colorFilterAnimation =
            new ValueCallbackKeyframeAnimation<>((LottieValueCallback<ColorFilter>) callback);
        colorFilterAnimation.addUpdateListener(this);
        layer.addAnimation(colorFilterAnimation);
      }
    } else if (property == LottieProperty.BLUR_RADIUS) {
      if (blurAnimation != null) {
        blurAnimation.setValueCallback((LottieValueCallback<Float>) callback);
      } else {
        blurAnimation =
            new ValueCallbackKeyframeAnimation<>((LottieValueCallback<Float>) callback);
        blurAnimation.addUpdateListener(this);
        layer.addAnimation(blurAnimation);
      }
    } else if (property == LottieProperty.DROP_SHADOW_COLOR && dropShadowAnimation != null) {
      dropShadowAnimation.setColorCallback((LottieValueCallback<Integer>) callback);
    } else if (property == LottieProperty.DROP_SHADOW_OPACITY && dropShadowAnimation != null) {
      dropShadowAnimation.setOpacityCallback((LottieValueCallback<Float>) callback);
    } else if (property == LottieProperty.DROP_SHADOW_DIRECTION && dropShadowAnimation != null) {
      dropShadowAnimation.setDirectionCallback((LottieValueCallback<Float>) callback);
    } else if (property == LottieProperty.DROP_SHADOW_DISTANCE && dropShadowAnimation != null) {
      dropShadowAnimation.setDistanceCallback((LottieValueCallback<Float>) callback);
    } else if (property == LottieProperty.DROP_SHADOW_RADIUS && dropShadowAnimation != null) {
      dropShadowAnimation.setRadiusCallback((LottieValueCallback<Float>) callback);
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
