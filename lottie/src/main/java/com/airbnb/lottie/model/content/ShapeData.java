package com.airbnb.lottie.model.content;

import android.graphics.PointF;
import android.support.annotation.FloatRange;
import android.util.JsonReader;
import android.util.JsonToken;

import com.airbnb.lottie.model.CubicCurveData;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.utils.JsonUtils;
import com.airbnb.lottie.utils.MiscUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShapeData {
  private final List<CubicCurveData> curves = new ArrayList<>();
  private PointF initialPoint;
  private boolean closed;

  private ShapeData(PointF initialPoint, boolean closed, List<CubicCurveData> curves) {
    this.initialPoint = initialPoint;
    this.closed = closed;
    this.curves.addAll(curves);
  }

  public ShapeData() {
  }

  private void setInitialPoint(float x, float y) {
    if (initialPoint == null) {
      initialPoint = new PointF();
    }
    initialPoint.set(x, y);
  }

  public PointF getInitialPoint() {
    return initialPoint;
  }

  public boolean isClosed() {
    return closed;
  }

  public List<CubicCurveData> getCurves() {
    return curves;
  }

  public void interpolateBetween(ShapeData shapeData1, ShapeData shapeData2,
      @FloatRange(from = 0f, to = 1f) float percentage) {
    if (initialPoint == null) {
      initialPoint = new PointF();
    }
    closed = shapeData1.isClosed() || shapeData2.isClosed();

    if (!curves.isEmpty() && curves.size() != shapeData1.getCurves().size()
        && curves.size() != shapeData2.getCurves().size()) {
      throw new IllegalStateException("Curves must have the same number of control points. This: "
          + getCurves().size()
          + "\tShape 1: " + shapeData1.getCurves().size() + "\tShape 2: "
          + shapeData2.getCurves().size());
    } else if (curves.isEmpty()) {
      for (int i = shapeData1.getCurves().size() - 1; i >= 0; i--) {
        curves.add(new CubicCurveData());
      }
    }

    PointF initialPoint1 = shapeData1.getInitialPoint();
    PointF initialPoint2 = shapeData2.getInitialPoint();

    setInitialPoint(MiscUtils.lerp(initialPoint1.x, initialPoint2.x, percentage),
        MiscUtils.lerp(initialPoint1.y, initialPoint2.y, percentage));

    for (int i = curves.size() - 1; i >= 0; i--) {
      CubicCurveData curve1 = shapeData1.getCurves().get(i);
      CubicCurveData curve2 = shapeData2.getCurves().get(i);

      PointF cp11 = curve1.getControlPoint1();
      PointF cp21 = curve1.getControlPoint2();
      PointF vertex1 = curve1.getVertex();

      PointF cp12 = curve2.getControlPoint1();
      PointF cp22 = curve2.getControlPoint2();
      PointF vertex2 = curve2.getVertex();

      curves.get(i).setControlPoint1(
          MiscUtils.lerp(cp11.x, cp12.x, percentage), MiscUtils.lerp(cp11.y, cp12.y,
              percentage));
      curves.get(i).setControlPoint2(
          MiscUtils.lerp(cp21.x, cp22.x, percentage), MiscUtils.lerp(cp21.y, cp22.y,
              percentage));
      curves.get(i).setVertex(
          MiscUtils.lerp(vertex1.x, vertex2.x, percentage), MiscUtils.lerp(vertex1.y, vertex2.y,
              percentage));
    }
  }

  @Override public String toString() {
    return "ShapeData{" + "numCurves=" + curves.size() +
        "closed=" + closed +
        '}';
  }

  public static class Factory implements AnimatableValue.Factory<ShapeData> {
    public static final ShapeData.Factory INSTANCE = new Factory();

    private Factory() {
    }

    @Override public ShapeData valueFromObject(JsonReader reader, float scale) throws IOException {
      // Sometimes the points data is in a array of length 1. Sometimes the data is at the top
      // level.
      if (reader.peek() == JsonToken.BEGIN_ARRAY) {
        reader.beginArray();
      }

      boolean closed = true;
      List<PointF> pointsArray = null;
      List<PointF> inTangents = null;
      List<PointF> outTangents = null;
      reader.beginObject();

      while (reader.peek() == JsonToken.NAME) {
        String name = reader.nextName();
        switch (name) {
          case "c":
            closed = reader.nextBoolean();
            break;
          case "v":
            pointsArray =  JsonUtils.jsonToPoints(reader, scale);
            break;
          case "i":
            inTangents =  JsonUtils.jsonToPoints(reader, scale);
            break;
          case "o":
            outTangents =  JsonUtils.jsonToPoints(reader, scale);
            break;
        }
      }

      reader.endObject();

      if (reader.peek() == JsonToken.END_ARRAY) {
        reader.endArray();
      }

      if (pointsArray == null || inTangents == null || outTangents == null) {
        throw new IllegalArgumentException("Shape data was missing information.");
      }

      if (pointsArray.isEmpty()) {
        return new ShapeData(new PointF(), false, Collections.<CubicCurveData>emptyList());
      }

      int length = pointsArray.size();
      PointF vertex = pointsArray.get(0);
      PointF initialPoint = vertex;
      List<CubicCurveData> curves = new ArrayList<>(length);

      for (int i = 1; i < length; i++) {
        vertex = pointsArray.get(i);
        PointF previousVertex = pointsArray.get(i - 1);
        PointF cp1 = outTangents.get(i - 1);
        PointF cp2 = inTangents.get(i);
        PointF shapeCp1 = MiscUtils.addPoints(previousVertex, cp1);
        PointF shapeCp2 = MiscUtils.addPoints(vertex, cp2);
        curves.add(new CubicCurveData(shapeCp1, shapeCp2, vertex));
      }

      if (closed) {
        vertex = pointsArray.get(0);
        PointF previousVertex = pointsArray.get(length - 1);
        PointF cp1 = outTangents.get(length - 1);
        PointF cp2 = inTangents.get(0);

        PointF shapeCp1 = MiscUtils.addPoints(previousVertex, cp1);
        PointF shapeCp2 = MiscUtils.addPoints(vertex, cp2);

        curves.add(new CubicCurveData(shapeCp1, shapeCp2, vertex));
      }
      return new ShapeData(initialPoint, closed, curves);
    }
  }
}
