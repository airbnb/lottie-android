package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StrokeContent implements Content, DrawingContent {

  private final PathMeasure pathMeasure = new PathMeasure();
  private final Path tempPath = new Path();
  private final Path tempPath2 = new Path();
  private final Path path = new Path();
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final LottieDrawable lottieDrawable;
  @Nullable private TrimPathContent trimPath;
  private final List<PathContent> paths = new ArrayList<>();

  private final BaseKeyframeAnimation<?, Integer> colorAnimation;
  private final BaseKeyframeAnimation<?, Float> widthAnimation;
  private final BaseKeyframeAnimation<?, Integer> opacityAnimation;
  private final List<BaseKeyframeAnimation<?, Float>> dashPatternAnimations;
  @Nullable private final BaseKeyframeAnimation<?, Float> offsetAnimation;

  StrokeContent(final LottieDrawable lottieDrawable, AnimatableLayer layer, ShapeStroke stroke) {
    this.lottieDrawable = lottieDrawable;
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
    for (int i = 0; i < contentsBefore.size(); i++) {
      Content content = contentsBefore.get(i);
      if (content instanceof TrimPathContent) {
        trimPath = (TrimPathContent) content;
        trimPath.addListener(new BaseKeyframeAnimation.SimpleAnimationListener() {
          @Override public void onValueChanged() {
            lottieDrawable.invalidateSelf();
          }
        });
      }
    }

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
      Path path = paths.get(i).getPath();
      path = applyTrimPathIfNeeded(path);
      this.path.addPath(path, parentMatrix);
    }

    canvas.drawPath(path, paint);
  }

  private Path applyTrimPathIfNeeded(Path path) {
    if (trimPath == null) {
      return path;
    }

    pathMeasure.setPath(path, false);

    float length = pathMeasure.getLength();
    float start = length * trimPath.getStart().getValue() / 100f;
    float end = length * trimPath.getEnd().getValue() / 100f;
    float newStart = Math.min(start, end);
    float newEnd = Math.max(start, end);

    float offset = trimPath.getOffset().getValue() / 360f * length;
    newStart += offset;
    newEnd += offset;

    // If the trim path has rotated around the path, we need to shift it back.
    if (newStart > length && newEnd > length) {
      newStart %= length;
      newEnd %= length;
    }
    if (newStart > newEnd) {
      newStart -= length;
    }

    tempPath.reset();
    pathMeasure.getSegment(
        newStart,
        newEnd,
        tempPath,
        true);

    if (newEnd > length) {
      tempPath2.reset();
      pathMeasure.getSegment(
          0,
          newEnd % length,
          tempPath2,
          true);
      tempPath.addPath(tempPath2);
    } else if (newStart < 0) {
      tempPath2.reset();
      pathMeasure.getSegment(
          length + newStart,
          length,
          tempPath2,
          true);
      tempPath.addPath(tempPath2);
    }
    return tempPath;
  }
}
