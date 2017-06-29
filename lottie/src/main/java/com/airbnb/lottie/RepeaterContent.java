package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class RepeaterContent implements
    DrawingContent, PathContent, GreedyContent, BaseKeyframeAnimation.AnimationListener {
  private final Matrix matrix = new Matrix();
  private final Path path = new Path();

  private final LottieDrawable lottieDrawable;
  private final BaseLayer layer;
  private final String name;
  private final KeyframeAnimation<Float> copies;
  private final KeyframeAnimation<Float> offset;
  private final TransformKeyframeAnimation transform;
  private ContentGroup contentGroup;


  RepeaterContent(LottieDrawable lottieDrawable, BaseLayer layer, Repeater repeater) {
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
    Matrix transform = this.transform.getMatrix();
    matrix.reset();
    path.reset();
    Path path = contentGroup.getPath();
    float copies = this.copies.getValue();
    for (int i = 0; i < copies; i++) {
      path.addPath(path, matrix);
      matrix.preConcat(transform);
    }
    return path;
  }

  @Override public void draw(Canvas canvas, Matrix parentMatrix, int alpha) {
    Matrix transform = this.transform.getMatrix();
    matrix.set(parentMatrix);
    float copies = this.copies.getValue();
    for (int i = 0; i < copies; i++) {
      contentGroup.draw(canvas, matrix, alpha);
      matrix.preConcat(transform);
    }
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix) {

  }

  @Override public void addColorFilter(@Nullable String layerName, @Nullable String contentName,
      @Nullable ColorFilter colorFilter) {

  }

  @Override public void onValueChanged() {
    lottieDrawable.invalidateSelf();
  }
}
