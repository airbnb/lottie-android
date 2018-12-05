package com.airbnb.lottie.animation.canvas;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.airbnb.lottie.L;

import java.util.ArrayList;
import java.util.List;

public class RecordedCanvas implements ICanvas {

  // TODO: create a Path Pool to avoid memory thrashing.
  private final List<Path> paths = new ArrayList<>();

  @Override
  public int save() {
    L.warn("RecordedCanvas cannot be saved");
    return 0;
  }

  @Override
  public int saveLayer(@Nullable RectF bounds, @Nullable Paint paint) {
    L.warn("RecordedCanvas cannot be saved");
    return 0;
  }

  @Override
  public int saveLayer(@Nullable RectF bounds, @Nullable Paint paint, int saveFlags) {
    L.warn("RecordedCanvas cannot be saved");
    return 0;
  }

  @Override
  public void translate(float dx, float dy) {
    Log.d("Gabe", "translate\t");
    // TODO
  }

  @Override
  public void scale(float sx, float sy, float px, float py) {
    Log.d("Gabe", "scale\t");
    // TODO
  }

  @Override
  public void restore() {
    L.warn("RecordedCanvas cannot be restored");
  }

  @Override
  public int getWidth() {
    // TODO
    return 0;
  }

  @Override
  public int getHeight() {
    // TODO
    return 0;
  }

  @Override
  public void drawPath(Path path, Paint paint) {
    // TODO. Miter? We can't use miter style for masks and mattes though.
    if (paint.getStyle() == Paint.Style.STROKE) {
      RectF bounds = new RectF();
      path.computeBounds(bounds, false);
      float scaleFactor = paint.getStrokeWidth() / bounds.width();
      Matrix largeMatrix = new Matrix();
      // TODO: use the right amount.
      largeMatrix.preScale(1f + scaleFactor, 1f + scaleFactor, bounds.width() / 2f, bounds.height() / 2f);
      Path largePath = new Path();
      largePath.addPath(path, largeMatrix);
      Matrix smallMatrix = new Matrix();
      // TODO: use the right amount.
      smallMatrix.preScale(1f - scaleFactor, 1f - scaleFactor, bounds.width() / 2f, bounds.height() / 2f);
      smallMatrix.preTranslate(paint.getStrokeWidth(), paint.getStrokeWidth());
      Path smallPath = new Path();
      smallPath.addPath(path, smallMatrix);

//      Path strokedPath = new Path();
//      strokedPath.op(largePath, smallPath, Path.Op.DIFFERENCE);

      Path strokedPath = new Path();
      strokedPath.set(largePath);
      strokedPath.setFillType(Path.FillType.EVEN_ODD);
      strokedPath.addPath(smallPath);


      paths.add(strokedPath);
    } else {
      paths.add(path);
    }
  }

  @Override
  public boolean clipPath(Path path) {
    // TODO
    return false;
  }

  @Override
  public boolean clipRect(RectF rect) {
    // TODO
    return false;
  }

  @Override
  public void drawText(char[] text, int index, int count, float x, float y, Paint paint) {
    // TODO
  }

  @Override
  public void setMatrix(@Nullable Matrix matrix) {
    Log.d("Gabe", "setMatrix\t");
    // TODO
  }

  @Override
  public void concat(@Nullable Matrix matrix) {
    // TODO
  }

  @Override
  public void drawBitmap(@NonNull Bitmap bitmap, Rect src, Rect dst, @Nullable Paint paint) {
    // TODO
  }

  public List<Path> getPaths() {
    return paths;
  }
}
