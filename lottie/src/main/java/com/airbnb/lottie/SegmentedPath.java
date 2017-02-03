package com.airbnb.lottie;

import android.graphics.Path;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

class SegmentedPath {
  private final List<Path> segments = new ArrayList<>();
  private final PointF currentPoint = new PointF();

  void moveTo(float x, float y) {
    currentPoint.set(x, y);
  }

  void lineTo(float x, float y) {
    Path path = new Path();
    // This is a small hack that ensures that an actual path will get created.
    // Without this, a lineTo call to the current point will end up creating an empty path
    // which breaks animations.
    if (currentPoint.x == x && currentPoint.y == y) {
      x += 0.01f;
      y += 0.01f;
    }
    path.moveTo(currentPoint.x, currentPoint.y);
    path.lineTo(x, y);
    segments.add(path);
    currentPoint.set(x, y);
  }

  void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {
    Path path = new Path();
    path.moveTo(currentPoint.x, currentPoint.y);
    path.cubicTo(x1, y1, x2, y2, x3, y3);
    segments.add(path);
    currentPoint.set(x3, y3);
  }

  Path getSegment(int index) {
    return segments.get(index);
  }

  int getSegmentCount() {
    return segments.size();
  }

  boolean hasSegments() {
    return !segments.isEmpty();
  }
}
