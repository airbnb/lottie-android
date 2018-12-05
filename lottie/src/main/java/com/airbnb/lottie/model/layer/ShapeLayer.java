package com.airbnb.lottie.model.layer;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.canvas.ICanvas;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.ContentGroup;
import com.airbnb.lottie.animation.keyframe.MaskKeyframeAnimation;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.content.ShapeGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShapeLayer extends BaseLayer {
  private final Path path = new Path();
  private final ContentGroup contentGroup;
  private List<Path> paths = Collections.emptyList();

  ShapeLayer(LottieDrawable lottieDrawable, Layer layerModel) {
    super(lottieDrawable, layerModel);

    // Naming this __container allows it to be ignored in KeyPath matching.
    ShapeGroup shapeGroup = new ShapeGroup("__container", layerModel.getShapes());
    contentGroup = new ContentGroup(lottieDrawable, this, shapeGroup);
    contentGroup.setContents(Collections.<Content>emptyList(), Collections.<Content>emptyList());
  }

  @Override void drawLayer(@NonNull ICanvas canvas, Matrix parentMatrix, int parentAlpha, @Nullable MaskKeyframeAnimation mask, Matrix maskMatrix, Matrix matteMatrix) {
    contentGroup.draw(canvas, parentMatrix, parentAlpha, mask, maskMatrix, matteMatrix);
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix) {
    super.getBounds(outBounds, parentMatrix);
    contentGroup.getBounds(outBounds, boundsMatrix);
  }

  @Override
  public Path getPath() {
    path.set(contentGroup.getPath());
    path.transform(getTransformMatrix());
    return path;
  }

  @Override
  public List<Path> getPaths() {
    List<Path> contentGroupPaths = contentGroup.getPaths();
    if (paths.size() != contentGroupPaths.size()) {
      paths = new ArrayList<>(contentGroupPaths.size());
      for (int i = 0; i < contentGroupPaths.size(); i++) {
        paths.add(new Path());
      }
    }
    Matrix matrix = getTransformMatrix();
    for (int i = 0; i < paths.size(); i++) {
      contentGroupPaths.get(i).transform(matrix, paths.get(i));
    }

    return paths;
  }

  @Override
  protected void resolveChildKeyPath(KeyPath keyPath, int depth, List<KeyPath> accumulator,
      KeyPath currentPartialKeyPath) {
    contentGroup.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath);
  }
}
