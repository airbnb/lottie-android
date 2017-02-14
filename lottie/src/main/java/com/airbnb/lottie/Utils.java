package com.airbnb.lottie;

import android.graphics.Path;
import android.graphics.PointF;

final class Utils {
  private static PointF emptyPoint;

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
}
