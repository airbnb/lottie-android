package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;

interface DrawingContent extends Content {
  void draw(Canvas canvas, Matrix parentMatrix, int alpha);
  void getBounds(RectF outBounds, Matrix parentMatrix);
}
