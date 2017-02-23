package com.airbnb.lottie;

import android.graphics.Path;

import java.io.Closeable;

final class Utils {
  private static CPointF emptyPoint;

  static CPointF emptyPoint() {
    if (emptyPoint == null) {
      emptyPoint = new CPointF();
    }
    return emptyPoint;
  }

  static Path createPath(CPointF startPoint, CPointF endPoint, CPointF cp1, CPointF cp2) {
    Path path = new Path();
    path.moveTo(startPoint.x(), startPoint.y());

    if (cp1 != null && cp1.length() != 0 && cp2 != null && cp2.length() != 0) {
      path.cubicTo(
          startPoint.x() + cp1.x(), startPoint.y() + cp1.y(),
          endPoint.x() + cp2.x(), endPoint.y() + cp2.y(),
          endPoint.x(), endPoint.y());
    } else {
      path.lineTo(endPoint.x(), endPoint.y());
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
}
