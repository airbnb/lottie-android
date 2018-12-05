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
import com.airbnb.lottie.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class RecordedCanvas implements ICanvas {

  private final int width;
  private final int height;

  // TODO: create a Path Pool to avoid memory thrashing.
  private final List<Path> paths = new ArrayList<>();

  public RecordedCanvas(int width, int height) {
    this.width = width;
    this.height = height;
  }


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
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public void drawPath(Path path, Paint paint) {
    // TODO. Miter? We can't use miter style for masks and mattes though.
    if (paint.getStyle() == Paint.Style.STROKE) {
      RectF bounds = new RectF();
      path.computeBounds(bounds, false);
      float cx = bounds.left + bounds.width() / 2f;
      float cy = bounds.top + bounds.height() / 2f;

      float scaleFactor = paint.getStrokeWidth() / bounds.width();
      Matrix largeMatrix = new Matrix();
      largeMatrix.preScale(
          1f + scaleFactor,
          1f + scaleFactor
      );
      Path largePath = new Path();
      largePath.addPath(path, largeMatrix);
      RectF largeBounds = Utils.getBounds(largePath);
      float cxLarge = largeBounds.left + largeBounds.width() / 2f;
      float cyLarge = largeBounds.top + largeBounds.height() / 2f;
      float dxLarge = cx - cxLarge;
      float dyLarge = cy - cyLarge;
      largeMatrix.reset();
      largeMatrix.preTranslate(dxLarge, dyLarge);
      largePath.transform(largeMatrix);


      Matrix smallMatrix = new Matrix();
      smallMatrix.preScale(
          1f - scaleFactor,
          1f - scaleFactor
      );
      Path smallPath = new Path();
      smallPath.addPath(path, smallMatrix);
      RectF smallBounds = Utils.getBounds(smallPath);
      float cxSmall = smallBounds.left + smallBounds.width() / 2f;
      float cySmall = smallBounds.top + smallBounds.height() / 2f;
      float dxSmall = cx - cxSmall;
      float dySmall = cy - cySmall;
      smallMatrix.reset();
      smallMatrix.preTranslate(dxSmall, dySmall);
      smallPath.transform(smallMatrix);

//      Path strokedPath = new Path();
//      strokedPath.set(largePath);
//      strokedPath.setFillType(Path.FillType.EVEN_ODD);
//      strokedPath.addPath(smallPath);
//      paths.add(strokedPath);

      largePath.op(smallPath, Path.Op.DIFFERENCE);
      paths.add(largePath);
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
    return !rect.isEmpty();
  }

  @Override
  public void drawText(char[] text, int index, int count, float x, float y, Paint paint) {
    // TODO
  }

  @Override
  public void drawRect(float left, float top, float right, float bottom, Paint paint) {
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
