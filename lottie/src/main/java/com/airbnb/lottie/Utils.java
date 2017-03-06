package com.airbnb.lottie;

import android.content.Context;
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
}
