package com.airbnb.lotte.utils;

import android.graphics.Path;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

public class SegmentedPath {

    private final List<Path> segments = new ArrayList<>();
    private final PointF currentPoint = new PointF();

    public void moveTo(float x, float y) {
        currentPoint.set(x, y);
    }

    public void lineTo(float x, float y) {
        Path path = new Path();
        path.moveTo(currentPoint.x, currentPoint.y);
        path.lineTo(x, y);
        currentPoint.set(x, y);
    }

    public void cubicTo (float x1, float y1, float x2, float y2, float x3, float y3) {
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

    public boolean hasSegments() {
        return !segments.isEmpty();
    }
}
