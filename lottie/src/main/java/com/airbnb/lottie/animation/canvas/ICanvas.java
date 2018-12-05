package com.airbnb.lottie.animation.canvas;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ICanvas {
  int save();
  int saveLayer(@Nullable RectF bounds, @Nullable Paint paint);
  int saveLayer(@Nullable RectF bounds, @Nullable Paint paint, int saveFlags);
  void translate(float dx, float dy);
  void scale(float sx, float sy, float px, float py);
  void restore();
  int getWidth();
  int getHeight();
  void drawPath(Path path, Paint paint);
  boolean clipPath(Path path);
  boolean clipRect(RectF rect);
  void drawText(char[] text, int index, int count, float x, float y, Paint paint);
  void drawRect(float left, float top, float right, float bottom, Paint paint);
  void setMatrix(@Nullable Matrix matrix);
  void concat(@Nullable Matrix matrix);
  void drawBitmap(@NonNull Bitmap bitmap, Rect src, Rect dst, @Nullable Paint paint);
}
