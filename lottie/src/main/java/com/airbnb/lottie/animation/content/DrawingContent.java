package com.airbnb.lottie.animation.content;

import android.graphics.Matrix;
import android.graphics.RectF;
import androidx.annotation.Nullable;
import com.airbnb.lottie.animation.canvas.ICanvas;
import com.airbnb.lottie.animation.keyframe.MaskKeyframeAnimation;

public interface DrawingContent extends Content {
  void draw(ICanvas canvas, Matrix parentMatrix, int alpha, @Nullable MaskKeyframeAnimation mask, Matrix maskMatrix, Matrix matteMatrix);
  void getBounds(RectF outBounds, Matrix parentMatrix);
}
