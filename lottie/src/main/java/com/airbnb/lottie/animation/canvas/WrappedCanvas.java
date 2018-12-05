package com.airbnb.lottie.animation.canvas;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WrappedCanvas implements ICanvas{

  private final Canvas canvas;

  public WrappedCanvas(Canvas canvas) {
    this.canvas = canvas;
  }

  @Override
  public int save() {
    return canvas.save();
  }

  @Override
  public int saveLayer(@Nullable RectF bounds, @Nullable Paint paint) {
    return canvas.saveLayer(bounds, paint);
  }

  @Override
  public int saveLayer(@Nullable RectF bounds, @Nullable Paint paint, int saveFlags) {
    return canvas.saveLayer(bounds, paint, saveFlags);
  }

  @Override
  public void translate(float dx, float dy) {
    canvas.translate(dx, dy);
  }

  @Override
  public void scale(float sx, float sy, float px, float py) {
    canvas.scale(sx, sy, px, py);
  }

  @Override
  public void restore() {
    canvas.restore();
  }

  @Override
  public int getWidth() {
    return canvas.getWidth();
  }

  @Override
  public int getHeight() {
    return canvas.getHeight();
  }

  @Override
  public void drawPath(Path path, Paint paint) {
    canvas.drawPath(path, paint);
  }

  @Override
  public boolean clipPath(Path path) {
    return canvas.clipPath(path);
  }

  @Override
  public boolean clipRect(RectF rect) {
    return canvas.clipRect(rect);
  }

  @Override
  public void drawText(char[] text, int index, int count, float x, float y, Paint paint) {
    canvas.drawText(text, index, count, x, y, paint);
  }

  @Override
  public void drawRect(float left, float top, float right, float bottom, Paint paint) {
    canvas.drawRect(left, top, right, bottom, paint);
  }

  @Override
  public void setMatrix(@Nullable Matrix matrix) {
    canvas.setMatrix(matrix);
  }

  @Override
  public void concat(@Nullable Matrix matrix) {
    canvas.concat(matrix);
  }

  @Override
  public void drawBitmap(@NonNull Bitmap bitmap, Rect src, Rect dst, @Nullable Paint paint) {
    canvas.drawBitmap(bitmap, src, dst, paint);
  }
}
