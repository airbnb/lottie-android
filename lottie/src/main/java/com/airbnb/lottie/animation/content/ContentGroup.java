package com.airbnb.lottie.animation.content;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.TransformKeyframeAnimation;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.model.content.ShapeGroup;
import com.airbnb.lottie.model.layer.BaseLayer;

import java.util.ArrayList;
import java.util.List;

public class ContentGroup implements DrawingContent, PathContent, TransformableContent,
    BaseKeyframeAnimation.AnimationListener {

  private static List<Content> contentsFromModels(LottieDrawable drawable, BaseLayer layer,
      List<ContentModel> contentModels) {
    List<Content> contents = new ArrayList<>(contentModels.size());
    for (int i = 0; i < contentModels.size(); i++) {
      Content content = contentModels.get(i).toContent(drawable, layer);
      if (content != null) {
        contents.add(content);
      }
    }
    return contents;
  }

  @Nullable static AnimatableTransform findTransform(List<ContentModel> contentModels) {
    for (int i = 0; i < contentModels.size(); i++) {
      ContentModel contentModel = contentModels.get(i);
      if (contentModel instanceof AnimatableTransform) {
        return (AnimatableTransform) contentModel;
      }
    }
    return null;
  }

  private final Matrix matrix = new Matrix();
  private final Path path = new Path();
  private final RectF rect = new RectF();

  private final String name;
  private final List<Content> contents;
  private final LottieDrawable lottieDrawable;
  @Nullable private List<PathContent> pathContents;
  @Nullable private TransformKeyframeAnimation transform;

  public ContentGroup(final LottieDrawable lottieDrawable, BaseLayer layer, ShapeGroup shapeGroup) {
    this(lottieDrawable, layer, shapeGroup.getName(),
        contentsFromModels(lottieDrawable, layer, shapeGroup.getItems()),
        findTransform(shapeGroup.getItems()));
  }

  ContentGroup(final LottieDrawable lottieDrawable, BaseLayer layer,
      String name, List<Content> contents, @Nullable AnimatableTransform transform) {
    this.name = name;
    this.lottieDrawable = lottieDrawable;
    this.contents = contents;

    if (transform != null) {
      this.transform = transform.createAnimation();
      this.transform.addAnimationsToLayer(layer);
      this.transform.addListener(this);
    }

    List<GreedyContent> greedyContents = new ArrayList<>();
    for (int i = contents.size() - 1; i >= 0; i--) {
      Content content = contents.get(i);
      if (content instanceof GreedyContent) {
        greedyContents.add((GreedyContent) content);
      }
    }

    for (int i = greedyContents.size() - 1; i >= 0; i--) {
      greedyContents.get(i).absorbContent(contents.listIterator(contents.size()));
    }
  }

  @Override public void onValueChanged() {
    lottieDrawable.invalidateSelf();
  }

  @Override public String getName() {
    return name;
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    // Do nothing with contents after.
    List<Content> myContentsBefore = new ArrayList<>(contentsBefore.size() + contents.size());
    myContentsBefore.addAll(contentsBefore);

    for (int i = contents.size() - 1; i >= 0; i--) {
      Content content = contents.get(i);
      content.setContents(myContentsBefore, contents.subList(0, i));
      myContentsBefore.add(content);
    }
  }

  List<PathContent> getPathList() {
    if (pathContents == null) {
      pathContents = new ArrayList<>();
      for (int i = 0; i < contents.size(); i++) {
        Content content = contents.get(i);
        if (content instanceof PathContent) {
          pathContents.add((PathContent) content);
        }
      }
    }
    return pathContents;
  }

  Matrix getTransformationMatrix() {
    if (transform != null) {
      return transform.getMatrix();
    }
    matrix.reset();
    return matrix;
  }

  @Override public Path getPath() {
    // TODO: cache this somehow.
    matrix.reset();
    if (transform != null) {
      matrix.set(transform.getMatrix());
    }
    path.reset();
    for (int i = contents.size() - 1; i >= 0; i--) {
      Content content = contents.get(i);
      if (content instanceof PathContent) {
        path.addPath(((PathContent) content).getPath(), matrix);
      }
    }
    return path;
  }

  @Override public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    matrix.set(parentMatrix);
    int alpha;
    if (transform != null) {
      matrix.preConcat(transform.getMatrix());
      alpha =
          (int) ((transform.getOpacity().getValue() / 100f * parentAlpha / 255f) * 255);
    } else {
      alpha = parentAlpha;
    }


    for (int i = contents.size() - 1; i >= 0; i--) {
      Object content = contents.get(i);
      if (content instanceof DrawingContent) {
        ((DrawingContent) content).draw(canvas, matrix, alpha);
      }
    }
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix) {
    matrix.set(parentMatrix);
    if (transform != null) {
      matrix.preConcat(transform.getMatrix());
    }
    rect.set(0, 0, 0, 0);
    for (int i = contents.size() - 1; i >= 0; i--) {
      Content content = contents.get(i);
      if (content instanceof DrawingContent) {
        ((DrawingContent) content).getBounds(rect, matrix);
        if (outBounds.isEmpty()) {
          outBounds.set(rect);
        } else {
          outBounds.set(
              Math.min(outBounds.left, rect.left),
              Math.min(outBounds.top, rect.top),
              Math.max(outBounds.right, rect.right),
              Math.max(outBounds.bottom, rect.bottom)
          );
        }
      }
    }
  }

  @Nullable public Content contentFor(String name) {
    if (name.equals(getName())) {
      return this;
    }
    for (int i = 0; i < contents.size(); i++) {
      Content content = contents.get(i);
      if (name.equals(content.getName())) {
        return content;
      }
    }
    return null;
  }

  @Nullable @Override public TransformKeyframeAnimation getTransform() {
    return transform;
  }

  public List<Content> getContents() {
    return contents;
  }

  public void appendAllKeyPaths(StringBuilder sb, String prefix) {
    String newPrefix;
    if (prefix.isEmpty()) {
      newPrefix = getName();
    } else {
      newPrefix = prefix + "." + getName();
    }
    List<Content> contents = getContents();
    for (int i = 0; i < contents.size(); i++) {
      Content content = contents.get(i);
      if (content instanceof ContentGroup) {
        ((ContentGroup) content).appendAllKeyPaths(sb, newPrefix);
      } else {
        sb.append("\n").append(newPrefix).append(".").append(content.getName());
      }
    }
  }
}
