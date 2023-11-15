package com.airbnb.lottie.animation.content;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.model.content.ShapeTrimPath;
import com.airbnb.lottie.model.layer.BaseLayer;

import java.util.ArrayList;
import java.util.List;

public class TrimPathContent implements Content, DrawingContent, BaseKeyframeAnimation.AnimationListener {

  private final String name;
  private final boolean hidden;
  private final List<BaseKeyframeAnimation.AnimationListener> listeners = new ArrayList<>();
  private final ShapeTrimPath.Type type;
  private final BaseKeyframeAnimation<?, Float> startAnimation;
  private final BaseKeyframeAnimation<?, Float> endAnimation;
  private final BaseKeyframeAnimation<?, Float> offsetAnimation;
  private final List<PathContent> pathContents = new ArrayList<>();
  private final Path path = new Path();
  private final PathMeasure pathMeasure = new PathMeasure();

  private float totalLength = 0f;
  private float startLength = 0f;
  private float endLength = 0f;
  private float lengthConsumed = 0f;

  public TrimPathContent(BaseLayer layer, ShapeTrimPath trimPath) {
    name = trimPath.getName();
    hidden = trimPath.isHidden();
    type = trimPath.getType();
    startAnimation = trimPath.getStart().createAnimation();
    endAnimation = trimPath.getEnd().createAnimation();
    offsetAnimation = trimPath.getOffset().createAnimation();

    layer.addAnimation(startAnimation);
    layer.addAnimation(endAnimation);
    layer.addAnimation(offsetAnimation);

    startAnimation.addUpdateListener(this);
    endAnimation.addUpdateListener(this);
    offsetAnimation.addUpdateListener(this);
  }

  @Override public void onValueChanged() {
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).onValueChanged();
    }
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    for (int i = 0; i < contentsAfter.size(); i++) {
      Content c = contentsAfter.get(i);
      if (c instanceof PathContent) {
        pathContents.add((PathContent) c);
      }
    }
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix, boolean applyParents) {
  }

  public void consumeLength(float length) {
    lengthConsumed += length;
  }

  public float getLengthConsumed() {
    return lengthConsumed;
  }

  public float getStartLength() {
    return Math.max(startLength - lengthConsumed, 0f);
  }

  public float getEndLength() {
    return Math.max(endLength - lengthConsumed, 0);
  }

  @Override public void draw(Canvas canvas, Matrix parentMatrix, int alpha) {
    lengthConsumed = 0f;
    path.rewind();
    for (int i = 0; i < pathContents.size(); i++) {
      path.addPath(pathContents.get(i).getPath(), parentMatrix);
    }
    pathMeasure.setPath(path, false);
    float totalLength = pathMeasure.getLength();
    while (pathMeasure.nextContour()) {
      totalLength += pathMeasure.getLength();
    }

    startLength = totalLength * startAnimation.getValue() / 100f;
    float end = endAnimation.getValue() / 100f;
    float offset = offsetAnimation.getValue() / 360f;
    endLength = totalLength * (end + offset);
  }

  @Override public String getName() {
    return name;
  }

  void addListener(BaseKeyframeAnimation.AnimationListener listener) {
    listeners.add(listener);
  }

  ShapeTrimPath.Type getType() {
    return type;
  }

  public BaseKeyframeAnimation<?, Float> getStart() {
    return startAnimation;
  }

  public BaseKeyframeAnimation<?, Float> getEnd() {
    return endAnimation;
  }

  public BaseKeyframeAnimation<?, Float> getOffset() {
    return offsetAnimation;
  }

  public boolean isHidden() {
    return hidden;
  }
}
