package com.airbnb.lottie.animation.content;

import android.graphics.PointF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.model.CubicCurveData;
import com.airbnb.lottie.model.content.RoundedCorners;
import com.airbnb.lottie.model.content.ShapeData;
import com.airbnb.lottie.model.layer.BaseLayer;

import java.util.ArrayList;
import java.util.List;

public class RoundedCornersContent implements ShapeModifierContent, BaseKeyframeAnimation.AnimationListener {
  /**
   * Copied from:
   * https://github.com/airbnb/lottie-web/blob/bb71072a26e03f1ca993da60915860f39aae890b/player/js/utils/common.js#L47
   */
  private static final float ROUNDED_CORNER_MAGIC_NUMBER = 0.5519f;

  private final LottieDrawable lottieDrawable;
  private final String name;
  private final BaseKeyframeAnimation<Float, Float> roundedCorners;
  @Nullable private ShapeData shapeData;

  public RoundedCornersContent(LottieDrawable lottieDrawable, BaseLayer layer, RoundedCorners roundedCorners) {
    this.lottieDrawable = lottieDrawable;
    this.name = roundedCorners.getName();
    this.roundedCorners = roundedCorners.getCornerRadius().createAnimation();
    layer.addAnimation(this.roundedCorners);
    this.roundedCorners.addUpdateListener(this);
  }

  @Override public String getName() {
    return name;
  }

  @Override public void onValueChanged() {
    lottieDrawable.invalidateSelf();
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    // Do nothing.
  }

  public BaseKeyframeAnimation<Float, Float> getRoundedCorners() {
    return roundedCorners;
  }

  /**
   * Rounded corner algorithm:
   * Iterate through each vertex.
   * If a vertex is a sharp corner, it rounds it.
   * If a vertex has control points, it is already rounded, so it does nothing.
   * <p>
   * To round a vertex:
   * Split the vertex into two.
   * Move vertex 1 directly towards the previous vertex.
   * Set vertex 1's in control point to itself so it is not rounded on that side.
   * Extend vertex 1's out control point towards the original vertex.
   * <p>
   * Repeat for vertex 2:
   * Move vertex 2 directly towards the next vertex.
   * Set vertex 2's out point to itself so it is not rounded on that side.
   * Extend vertex 2's in control point towards the original vertex.
   * <p>
   * The distance that the vertices and control points are moved are relative to the
   * shape's vertex distances and the roundedness set in the animation.
   */
  @Override public ShapeData modifyShape(ShapeData startingShapeData) {
    List<CubicCurveData> startingCurves = startingShapeData.getCurves();
    if (startingCurves.size() <= 2) {
      return startingShapeData;
    }
    float roundedness = roundedCorners.getValue();
    if (roundedness == 0f) {
      return startingShapeData;
    }

    ShapeData modifiedShapeData = getShapeData(startingShapeData);
    modifiedShapeData.setInitialPoint(startingShapeData.getInitialPoint().x, startingShapeData.getInitialPoint().y);
    List<CubicCurveData> modifiedCurves = modifiedShapeData.getCurves();
    int modifiedCurvesIndex = 0;
    boolean isClosed = startingShapeData.isClosed();

    // i represents which vertex we are currently on. Refer to the docs of CubicCurveData prior to working with
    // this code.
    // When i == 0
    //    vertex=ShapeData.initialPoint
    //    inCp=if closed vertex else curves[size - 1].cp2
    //    outCp=curves[0].cp1
    // When i == 1
    //    vertex=curves[0].vertex
    //    inCp=curves[0].cp2
    //    outCp=curves[1].cp1.
    // When i == size - 1
    //    vertex=curves[size - 1].vertex
    //    inCp=curves[size - 1].cp2
    //    outCp=if closed vertex else curves[0].cp1
    for (int i = 0; i < startingCurves.size(); i++) {
      CubicCurveData startingCurve = startingCurves.get(i);
      CubicCurveData previousCurve = startingCurves.get(floorMod(i - 1, startingCurves.size()));
      CubicCurveData previousPreviousCurve = startingCurves.get(floorMod(i - 2, startingCurves.size()));
      PointF vertex = (i == 0 && !isClosed) ? startingShapeData.getInitialPoint() : previousCurve.getVertex();
      PointF inPoint = (i == 0 && !isClosed) ? vertex : previousCurve.getControlPoint2();
      PointF outPoint = startingCurve.getControlPoint1();
      PointF previousVertex = previousPreviousCurve.getVertex();
      PointF nextVertex = startingCurve.getVertex();

      // We can't round the corner of the end of a non-closed curve.
      boolean isEndOfCurve = !startingShapeData.isClosed() && (i == 0 || i == startingCurves.size() - 1);
      if (inPoint.equals(vertex) && outPoint.equals(vertex) && !isEndOfCurve) {
        // This vertex is a point. Round its corners
        float dxToPreviousVertex = vertex.x - previousVertex.x;
        float dyToPreviousVertex = vertex.y - previousVertex.y;
        float dxToNextVertex = nextVertex.x - vertex.x;
        float dyToNextVertex = nextVertex.y - vertex.y;

        float dToPreviousVertex = (float) Math.hypot(dxToPreviousVertex, dyToPreviousVertex);
        float dToNextVertex = (float) Math.hypot(dxToNextVertex, dyToNextVertex);

        float previousVertexPercent = Math.min(roundedness / dToPreviousVertex, 0.5f);
        float nextVertexPercent = Math.min(roundedness / dToNextVertex, 0.5f);

        // Split the vertex into two and move each vertex towards the previous/next vertex.
        float newVertex1X = vertex.x + (previousVertex.x - vertex.x) * previousVertexPercent;
        float newVertex1Y = vertex.y + (previousVertex.y - vertex.y) * previousVertexPercent;
        float newVertex2X = vertex.x + (nextVertex.x - vertex.x) * nextVertexPercent;
        float newVertex2Y = vertex.y + (nextVertex.y - vertex.y) * nextVertexPercent;

        // Extend the new vertex control point towards the original vertex.
        float newVertex1OutPointX = newVertex1X - (newVertex1X - vertex.x) * ROUNDED_CORNER_MAGIC_NUMBER;
        float newVertex1OutPointY = newVertex1Y - (newVertex1Y - vertex.y) * ROUNDED_CORNER_MAGIC_NUMBER;
        float newVertex2InPointX = newVertex2X - (newVertex2X - vertex.x) * ROUNDED_CORNER_MAGIC_NUMBER;
        float newVertex2InPointY = newVertex2Y - (newVertex2Y - vertex.y) * ROUNDED_CORNER_MAGIC_NUMBER;

        // Remap vertex/in/out point to CubicCurveData.
        // Refer to the docs for CubicCurveData for more info on the difference.
        CubicCurveData previousCurveData = modifiedCurves.get(floorMod(modifiedCurvesIndex - 1, modifiedCurves.size()));
        CubicCurveData currentCurveData = modifiedCurves.get(modifiedCurvesIndex);
        previousCurveData.setControlPoint2(newVertex1X, newVertex1Y);
        previousCurveData.setVertex(newVertex1X, newVertex1Y);
        if (i == 0) {
          modifiedShapeData.setInitialPoint(newVertex1X, newVertex1Y);
        }
        currentCurveData.setControlPoint1(newVertex1OutPointX, newVertex1OutPointY);
        modifiedCurvesIndex++;

        previousCurveData = currentCurveData;
        currentCurveData = modifiedCurves.get(modifiedCurvesIndex);
        previousCurveData.setControlPoint2(newVertex2InPointX, newVertex2InPointY);
        previousCurveData.setVertex(newVertex2X, newVertex2Y);
        currentCurveData.setControlPoint1(newVertex2X, newVertex2Y);
        modifiedCurvesIndex++;
      } else {
        // This vertex is not a point. Don't modify it. Refer to the documentation above and for CubicCurveData for mapping a vertex
        // oriented point to CubicCurveData (path segments).
        CubicCurveData previousCurveData = modifiedCurves.get(floorMod(modifiedCurvesIndex - 1, modifiedCurves.size()));
        CubicCurveData currentCurveData = modifiedCurves.get(modifiedCurvesIndex);
        previousCurveData.setControlPoint2(previousCurve.getControlPoint2().x, previousCurve.getControlPoint2().y);
        previousCurveData.setVertex(previousCurve.getVertex().x, previousCurve.getVertex().y);
        currentCurveData.setControlPoint1(startingCurve.getControlPoint1().x, startingCurve.getControlPoint1().y);
        modifiedCurvesIndex++;
      }
    }
    return modifiedShapeData;
  }

  /**
   * Returns a shape data with the correct number of vertices for the rounded corners shape.
   * This just returns the object. It does not update any values within the shape.
   */
  @NonNull
  private ShapeData getShapeData(ShapeData startingShapeData) {
    List<CubicCurveData> startingCurves = startingShapeData.getCurves();
    boolean isClosed = startingShapeData.isClosed();
    int vertices = 0;
    for (int i = startingCurves.size() - 1; i >= 0; i--) {
      CubicCurveData startingCurve = startingCurves.get(i);
      CubicCurveData previousCurve = startingCurves.get(floorMod(i - 1, startingCurves.size()));
      PointF vertex = (i == 0 && !isClosed) ? startingShapeData.getInitialPoint() : previousCurve.getVertex();
      PointF inPoint = (i == 0 && !isClosed) ? vertex : previousCurve.getControlPoint2();
      PointF outPoint = startingCurve.getControlPoint1();

      boolean isEndOfCurve = !startingShapeData.isClosed() && (i == 0 || i == startingCurves.size() - 1);
      if (inPoint.equals(vertex) && outPoint.equals(vertex) && !isEndOfCurve) {
        vertices += 2;
      } else {
        vertices += 1;
      }
    }
    if (shapeData == null || shapeData.getCurves().size() != vertices) {
      List<CubicCurveData> newCurves = new ArrayList<>(vertices);
      for (int i = 0; i < vertices; i++) {
        newCurves.add(new CubicCurveData());
      }
      shapeData = new ShapeData(new PointF(0f, 0f), false, newCurves);
    }
    shapeData.setClosed(isClosed);
    return shapeData;
  }

  /**
   * Copied from the API 24+ AOSP source.
   */
  private static int floorMod(int x, int y) {
    return x - floorDiv(x, y) * y;
  }

  /**
   * Copied from the API 24+ AOSP source.
   */
  private static int floorDiv(int x, int y) {
    int r = x / y;
    // if the signs are different and modulo not zero, round down
    if ((x ^ y) < 0 && (r * y != x)) {
      r--;
    }
    return r;
  }
}
