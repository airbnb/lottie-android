package com.airbnb.lottie.model.layer;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;

import androidx.annotation.Nullable;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.DropShadowKeyframeAnimation;
import com.airbnb.lottie.utils.DropShadow;

public class NullLayer extends BaseLayer {
  NullLayer(LottieDrawable lottieDrawable, Layer layerModel) {
    super(lottieDrawable, layerModel);
  }

  @Override void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha, @Nullable DropShadow shadowToApply) {
    // Do nothing.
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix, boolean applyParents) {
    super.getBounds(outBounds, parentMatrix, applyParents);
    outBounds.set(0, 0, 0, 0);
  }
}
