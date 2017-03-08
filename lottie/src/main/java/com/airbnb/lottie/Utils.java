package com.airbnb.lottie;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.Closeable;

final class Utils {
  private static PointF emptyPoint;
  private static DisplayMetrics displayMetrics;

  static PointF emptyPoint() {
    if (emptyPoint == null) {
      emptyPoint = new PointF();
    }
    return emptyPoint;
  }

  static Path createPath(PointF startPoint, PointF endPoint, PointF cp1, PointF cp2) {
    Path path = new Path();
    path.moveTo(startPoint.x, startPoint.y);

    if (cp1 != null && cp1.length() != 0 && cp2 != null && cp2.length() != 0) {
      path.cubicTo(
          startPoint.x + cp1.x, startPoint.y + cp1.y,
          endPoint.x + cp2.x, endPoint.y + cp2.y,
          endPoint.x, endPoint.y);
    } else {
      path.lineTo(endPoint.x, endPoint.y);
    }
    return path;
  }

  static void closeQuietly(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (RuntimeException rethrown) {
        throw rethrown;
      } catch (Exception ignored) {
      }
    }
  }

  static int getScreenWidth(Context context) {
    if (displayMetrics == null) {
      displayMetrics = new DisplayMetrics();
    }
    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    wm.getDefaultDisplay().getMetrics(displayMetrics);
    return displayMetrics.widthPixels;
  }

  static int getScreenHeight(Context context) {
    if (displayMetrics == null) {
      displayMetrics = new DisplayMetrics();
    }
    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    wm.getDefaultDisplay().getMetrics(displayMetrics);
    return displayMetrics.heightPixels;
  }

  static void applyTransformToCanvas(LottieDrawable drawable, TransformKeyframeAnimation transform,
      Canvas canvas) {

    float scale = drawable.getScale();

    PointF position = transform.getPosition().getValue();
    if (position.x != 0 || position.y != 0) {
      canvas.translate(position.x * scale, position.y * scale);
    }

    float rotation = transform.getRotation().getValue();
    if (rotation != 0f) {
      canvas.rotate(rotation);
    }

    ScaleXY scaleTransform = transform.getScale().getValue();
    if (scaleTransform.getScaleX() != 1f || scaleTransform.getScaleY() != 1f) {
      canvas.scale(scaleTransform.getScaleX(), scaleTransform.getScaleY());
    }

    PointF anchorPoint = transform.getAnchorPoint().getValue();
    if (anchorPoint.x != 0 || anchorPoint.y != 0) {
      canvas.translate(-anchorPoint.x * scale, -anchorPoint.y * scale);
    }
  }
}
