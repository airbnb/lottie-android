package com.airbnb.lottie.model.layer;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;

import androidx.annotation.Nullable;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.MaskKeyframeAnimation;

import java.util.Collections;
import java.util.List;

public class NullLayer extends BaseLayer {
  private final Path path = new Path();

  NullLayer(LottieDrawable lottieDrawable, Layer layerModel) {
    super(lottieDrawable, layerModel);
  }

  @Override void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha, @Nullable MaskKeyframeAnimation mask, Matrix maskMatrix, Matrix matteMatrix) {
    // Do nothing.
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix) {
    super.getBounds(outBounds, parentMatrix);
    outBounds.set(0, 0, 0, 0);
  }

  @Override
  public Path getPath() {
    return path;
  }

  @Override
  public List<Path> getPaths() {
    return Collections.emptyList();
  }
}
