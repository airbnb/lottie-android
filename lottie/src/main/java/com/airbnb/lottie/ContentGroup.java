package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ContentGroup implements DrawingContent, PathContent,
    BaseKeyframeAnimation.AnimationListener {
  private final Matrix matrix = new Matrix();
  private final Path path = new Path();

  private final List<Content> contents = new ArrayList<>();
  private final LottieDrawable lottieDrawable;
  @Nullable private List<PathContent> pathContents;
  @Nullable private TransformKeyframeAnimation transformAnimation;

  ContentGroup(final LottieDrawable lottieDrawable, BaseLayer layer, ShapeGroup shapeGroup) {
    this.lottieDrawable = lottieDrawable;
    List<Object> items = shapeGroup.getItems();
    if (items.isEmpty()) {
      return;
    }

    Object potentialTransform = items.get(items.size() - 1);
    if (potentialTransform instanceof AnimatableTransform) {
      transformAnimation = ((AnimatableTransform) potentialTransform).createAnimation();
      //noinspection ConstantConditions
      transformAnimation.addAnimationsToLayer(layer);
      transformAnimation.addListener(this);
    }

    for (int i = 0; i < items.size(); i++) {
      Object item = items.get(i);
      if (item instanceof ShapeFill) {
        contents.add(new FillContent(lottieDrawable, layer, (ShapeFill) item));
      } else if (item instanceof ShapeStroke) {
        contents.add(new StrokeContent(lottieDrawable, layer, (ShapeStroke) item));
      } else if (item instanceof ShapeGroup) {
        contents.add(new ContentGroup(lottieDrawable, layer, (ShapeGroup) item));
      } else if (item instanceof RectangleShape) {
        contents.add(new RectangleContent(lottieDrawable, layer, (RectangleShape) item));
      } else if (item instanceof CircleShape) {
        contents.add(new EllipseContent(lottieDrawable, layer, (CircleShape) item));
      } else if (item instanceof ShapePath) {
        contents.add(new ShapeContent(lottieDrawable, layer, (ShapePath) item));
      } else if (item instanceof PolystarShape) {
        contents.add(new PolystarContent(lottieDrawable, layer, (PolystarShape) item));
      } else if (item instanceof ShapeTrimPath) {
        contents.add(new TrimPathContent(layer, (ShapeTrimPath) item));
      } else //noinspection StatementWithEmptyBody
        if (item instanceof MergePaths) {
        // Merge paths are not ready yet.
        // contents.add(new MergePathsContent((MergePaths) item));
      }
    }

    List<Content> contentsToRemove = new ArrayList<>();
    MergePathsContent currentMergePathsContent = null;
    for (int i = contents.size() - 1; i >= 0; i--) {
      Content content = contents.get(i);
      if (content instanceof MergePathsContent) {
        currentMergePathsContent = (MergePathsContent) content;
      }
      if (currentMergePathsContent != null && content != currentMergePathsContent) {
        currentMergePathsContent.addContentIfNeeded(content);
        contentsToRemove.add(content);
      }
    }

    Iterator<Content> it = contents.iterator();
    while (it.hasNext()) {
      Content content = it.next();
      if (contentsToRemove.contains(content)) {
        it.remove();
      }
    }
  }

  @Override public void onValueChanged() {
    lottieDrawable.invalidateSelf();
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
    if (transformAnimation != null) {
      return transformAnimation.getMatrix();
    }
    matrix.reset();
    return matrix;
  }

  @Override public Path getPath() {
    // TODO: cache this somehow.
    matrix.reset();
    if (transformAnimation != null) {
      matrix.set(transformAnimation.getMatrix());
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
    if (transformAnimation != null) {
      matrix.preConcat(transformAnimation.getMatrix());
      alpha =
          (int) ((transformAnimation.getOpacity().getValue() / 100f * parentAlpha / 255f) * 255);
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
}
