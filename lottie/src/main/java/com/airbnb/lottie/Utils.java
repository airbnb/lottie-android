package com.airbnb.lottie;

import android.graphics.PointF;

final class Utils {
  private static PointF emptyPoint;

  static PointF emptyPoint() {
    if (emptyPoint == null) {
      emptyPoint = new PointF();
    }
    return emptyPoint;
  }
}
