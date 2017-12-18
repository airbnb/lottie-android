package com.airbnb.lottie.animation.content;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.content.RectangleShape;
import com.airbnb.lottie.model.content.ShapeTrimPath;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.LottieValueCallback;

import java.util.List;

public class RectangleContent
    implements BaseKeyframeAnimation.AnimationListener, KeyPathElementContent, PathContent {
  private final Path path = new Path();
  private final RectF rect = new RectF();

  private final String name;
  private final LottieDrawable lottieDrawable;
  private final BaseKeyframeAnimation<?, PointF> positionAnimation;
  private final BaseKeyframeAnimation<?, PointF> sizeAnimation;
  private final BaseKeyframeAnimation<?, Float> cornerRadiusAnimation;

  @Nullable private TrimPathContent trimPath;
  private boolean isPathValid;

  public RectangleContent(LottieDrawable lottieDrawable, BaseLayer layer, RectangleShape rectShape) {
    name = rectShape.getName();
    this.lottieDrawable = lottieDrawable;
    positionAnimation = rectShape.getPosition().createAnimation();
    sizeAnimation = rectShape.getSize().createAnimation();
    cornerRadiusAnimation = rectShape.getCornerRadius().createAnimation();

    layer.addAnimation(positionAnimation);
    layer.addAnimation(sizeAnimation);
    layer.addAnimation(cornerRadiusAnimation);

    positionAnimation.addUpdateListener(this);
    sizeAnimation.addUpdateListener(this);
    cornerRadiusAnimation.addUpdateListener(this);
  }

  @Override public String getName() {
    return name;
  }

  @Override public void onValueChanged() {
    invalidate();
  }

  private void invalidate() {
    isPathValid = false;
    lottieDrawable.invalidateSelf();
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    for (int i = 0; i < contentsBefore.size(); i++) {
      Content content = contentsBefore.get(i);
      if (content instanceof TrimPathContent &&
          ((TrimPathContent) content).getType() == ShapeTrimPath.Type.Simultaneously) {
        trimPath = (TrimPathContent) content;
        trimPath.addListener(this);
      }
    }
  }

  @Override public Path getPath() {
    if (isPathValid) {
      return path;
    }

    path.reset();

    float density = Utils.density();

    PointF size = sizeAnimation.getValue();
    float halfWidth = size.x * density / 2f;
    float halfHeight = size.y * density / 2f;
    float radius = cornerRadiusAnimation == null ? 0f : cornerRadiusAnimation.getValue();
    float maxRadius = Math.min(halfWidth, halfHeight);
    if (radius > maxRadius) {
      radius = maxRadius;
    }

    // Draw the rectangle top right to bottom left.
    PointF position = positionAnimation.getValue();
    float px = position.x * density;
    float py = position.y * density;

    path.moveTo(px + halfWidth, py - halfHeight + radius);

    path.lineTo(px + halfWidth, py + halfHeight - radius);

    if (radius > 0) {
      rect.set(px + halfWidth - 2 * radius,
          py + halfHeight - 2 * radius,
          px + halfWidth,
          py + halfHeight);
      path.arcTo(rect, 0, 90, false);
    }

    path.lineTo(px - halfWidth + radius, py + halfHeight);

    if (radius > 0) {
      rect.set(px - halfWidth,
          py + halfHeight - 2 * radius,
          px - halfWidth + 2 * radius,
          py + halfHeight);
      path.arcTo(rect, 90, 90, false);
    }

    path.lineTo(px - halfWidth, py - halfHeight + radius);

    if (radius > 0) {
      rect.set(px - halfWidth,
          py - halfHeight,
          px - halfWidth + 2 * radius,
          py - halfHeight + 2 * radius);
      path.arcTo(rect, 180, 90, false);
    }

    path.lineTo(px + halfWidth - radius, py - halfHeight);

    if (radius > 0) {
      rect.set(px + halfWidth - 2 * radius,
          py - halfHeight,
          px + halfWidth,
          py - halfHeight + 2 * radius);
      path.arcTo(rect, 270, 90, false);
    }
    path.close();

    Utils.applyTrimPathIfNeeded(path, trimPath);

    isPathValid = true;
    return path;
  }

  @Override public void resolveKeyPath(KeyPath keyPath, int depth, List<KeyPath> accumulator,
      KeyPath currentPartialKeyPath) {
    MiscUtils.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath, this);
  }

  @Override
  public <T> void addValueCallback(T property, @Nullable LottieValueCallback<T> callback) {

  }
}
