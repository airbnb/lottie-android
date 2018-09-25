package com.airbnb.lottie.animation.content;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.TransformKeyframeAnimation;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.content.Repeater;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.value.LottieValueCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class RepeaterContent implements DrawingContent, PathContent, GreedyContent,
    BaseKeyframeAnimation.AnimationListener, KeyPathElementContent {
  private final Matrix matrix = new Matrix();
  private final Path path = new Path();

  private final LottieDrawable lottieDrawable;
  private final BaseLayer layer;
  private final String name;
  private final BaseKeyframeAnimation<Float, Float> copies;
  private final BaseKeyframeAnimation<Float, Float> offset;
  private final TransformKeyframeAnimation transform;
  private ContentGroup contentGroup;


  public RepeaterContent(LottieDrawable lottieDrawable, BaseLayer layer, Repeater repeater) {
    this.lottieDrawable = lottieDrawable;
    this.layer = layer;
    name = repeater.getName();
    copies = repeater.getCopies().createAnimation();
    layer.addAnimation(copies);
    copies.addUpdateListener(this);

    offset = repeater.getOffset().createAnimation();
    layer.addAnimation(offset);
    offset.addUpdateListener(this);

    transform = repeater.getTransform().createAnimation();
    transform.addAnimationsToLayer(layer);
    transform.addListener(this);
  }

  @Override public void absorbContent(ListIterator<Content> contentsIter) {
    // This check prevents a repeater from getting added twice.
    // This can happen in the following situation:
    //    RECTANGLE
    //    REPEATER 1
    //    FILL
    //    REPEATER 2
    // In this case, the expected structure would be:
    //     REPEATER 2
    //        REPEATER 1
    //            RECTANGLE
    //        FILL
    // Without this check, REPEATER 1 will try and absorb contents once it is already inside of
    // REPEATER 2.
    if (contentGroup != null) {
      return;
    }
    // Fast forward the iterator until after this content.
    //noinspection StatementWithEmptyBody
    while (contentsIter.hasPrevious() && contentsIter.previous() != this) {}
    List<Content> contents = new ArrayList<>();
    while (contentsIter.hasPrevious()) {
      contents.add(contentsIter.previous());
      contentsIter.remove();
    }
    Collections.reverse(contents);
    contentGroup = new ContentGroup(lottieDrawable, layer, "Repeater", contents, null);
  }

  @Override public String getName() {
    return name;
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    contentGroup.setContents(contentsBefore, contentsAfter);
  }

  @Override public Path getPath() {
    Path contentPath = contentGroup.getPath();
    path.reset();
    float copies = this.copies.getValue();
    float offset = this.offset.getValue();
    for (int i = (int) copies - 1; i >= 0; i--) {
      matrix.set(transform.getMatrixForRepeater(i + offset));
      path.addPath(contentPath, matrix);
    }
    return path;
  }

  @Override public void draw(Canvas canvas, Matrix parentMatrix, int alpha) {
    float copies = this.copies.getValue();
    float offset = this.offset.getValue();
    //noinspection ConstantConditions
    float startOpacity = this.transform.getStartOpacity().getValue() / 100f;
    //noinspection ConstantConditions
    float endOpacity = this.transform.getEndOpacity().getValue() / 100f;
    for (int i = (int) copies - 1; i >= 0; i--) {
      matrix.set(parentMatrix);
      matrix.preConcat(transform.getMatrixForRepeater(i + offset));
      float newAlpha = alpha * MiscUtils.lerp(startOpacity, endOpacity, i / copies);
      contentGroup.draw(canvas, matrix, (int) newAlpha);
    }
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix) {
    contentGroup.getBounds(outBounds, parentMatrix);
  }

  @Override public void onValueChanged() {
    lottieDrawable.invalidateSelf();
  }

  @Override public void resolveKeyPath(
      KeyPath keyPath, int depth, List<KeyPath> accumulator, KeyPath currentPartialKeyPath) {
    MiscUtils.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath, this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> void addValueCallback(T property, @Nullable LottieValueCallback<T> callback) {
    if (transform.applyValueCallback(property, callback)) {
      return;
    }

    if (property == LottieProperty.REPEATER_COPIES) {
      copies.setValueCallback((LottieValueCallback<Float>) callback);
    } else if (property == LottieProperty.REPEATER_OFFSET) {
      offset.setValueCallback((LottieValueCallback<Float>) callback);
    }
  }
}
