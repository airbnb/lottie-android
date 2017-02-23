package com.airbnb.lottie;

import android.support.annotation.FloatRange;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class ShapeData {
    private final List<CubicCurveData> curves = new ArrayList<>();
    private CPointF initialPoint;
    private boolean closed;

    private ShapeData(CPointF initialPoint, boolean closed, List<CubicCurveData> curves) {
        this.initialPoint = initialPoint;
        this.closed = closed;
        this.curves.addAll(curves);
    }

    ShapeData() {
    }

    private void setInitialPoint(float x, float y) {
        if (initialPoint == null) {
            initialPoint = new CPointF();
        }
        initialPoint.set(x, y);
    }

    CPointF getInitialPoint() {
        return initialPoint;
    }

    boolean isClosed() {
        return closed;
    }

    List<CubicCurveData> getCurves() {
        return curves;
    }

    void interpolateBetween(ShapeData shapeData1, ShapeData shapeData2,
                            @FloatRange(from = 0f, to = 1f) float percentage) {
        if (initialPoint == null) {
            initialPoint = new CPointF();
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

        CPointF initialPoint1 = shapeData1.getInitialPoint();
        CPointF initialPoint2 = shapeData2.getInitialPoint();

        setInitialPoint(MiscUtils.lerp(initialPoint1.x(), initialPoint2.x(), percentage),
                MiscUtils.lerp(initialPoint1.y(), initialPoint2.y(), percentage));

        for (int i = curves.size() - 1; i >= 0; i--) {
            CubicCurveData curve1 = shapeData1.getCurves().get(i);
            CubicCurveData curve2 = shapeData2.getCurves().get(i);

            CPointF cp11 = curve1.getControlPoint1();
            CPointF cp21 = curve1.getControlPoint2();
            CPointF vertex1 = curve1.getVertex();

            CPointF cp12 = curve2.getControlPoint1();
            CPointF cp22 = curve2.getControlPoint2();
            CPointF vertex2 = curve2.getVertex();

            curves.get(i).setControlPoint1(
                    MiscUtils.lerp(cp11.x(), cp12.x(), percentage), MiscUtils.lerp(cp11.y(), cp12.y(),
                            percentage));
            curves.get(i).setControlPoint2(
                    MiscUtils.lerp(cp21.x(), cp22.x(), percentage), MiscUtils.lerp(cp21.y(), cp22.y(),
                            percentage));
            curves.get(i).setVertex(
                    MiscUtils.lerp(vertex1.x(), vertex2.x(), percentage), MiscUtils.lerp(vertex1.y(), vertex2.y(),
                            percentage));
        }
    }

    @Override
    public String toString() {
        return "ShapeData{" + "numCurves=" + curves.size() +
                "closed=" + closed +
                '}';
    }

    static class Factory {
        private Factory() {
        }

        static ShapeData newInstance(Object object, float scale) {
            JSONObject pointsData = null;
            if (object instanceof JSONArray) {
                Object firstObject = ((JSONArray) object).opt(0);
                if (firstObject instanceof JSONObject && ((JSONObject) firstObject).has("v")) {
                    pointsData = (JSONObject) firstObject;
                }
            } else if (object instanceof JSONObject && ((JSONObject) object).has("v")) {
                pointsData = (JSONObject) object;
            }

            if (pointsData == null) {
                return null;
            }

            JSONArray pointsArray = pointsData.optJSONArray("v");
            JSONArray inTangents = pointsData.optJSONArray("i");
            JSONArray outTangents = pointsData.optJSONArray("o");
            boolean closed = pointsData.optBoolean("c", false);

            if (pointsArray == null || inTangents == null || outTangents == null ||
                    pointsArray.length() != inTangents.length() ||
                    pointsArray.length() != outTangents.length()) {
                throw new IllegalStateException(
                        "Unable to process points array or tangents. " + pointsData);
            }

            int length = pointsArray.length();
            CPointF vertex = vertexAtIndex(0, pointsArray);
            vertex.scaleX(scale);
            vertex.scaleY(scale);
            CPointF initialPoint = vertex;
            List<CubicCurveData> curves = new ArrayList<>(length);

            for (int i = 1; i < length; i++) {
                vertex = vertexAtIndex(i, pointsArray);
                CPointF previousVertex = vertexAtIndex(i - 1, pointsArray);
                CPointF cp1 = vertexAtIndex(i - 1, outTangents);
                CPointF cp2 = vertexAtIndex(i, inTangents);
                CPointF shapeCp1 = MiscUtils.addPoints(previousVertex, cp1);
                CPointF shapeCp2 = MiscUtils.addPoints(vertex, cp2);

                shapeCp1.scaleX(scale);
                shapeCp1.scaleY(scale);
                shapeCp2.scaleX(scale);
                shapeCp2.scaleY(scale);
                vertex.scaleX(scale);
                vertex.scaleY(scale);

                curves.add(new CubicCurveData(shapeCp1, shapeCp2, vertex));
            }

            if (closed) {
                vertex = vertexAtIndex(0, pointsArray);
                CPointF previousVertex = vertexAtIndex(length - 1, pointsArray);
                CPointF cp1 = vertexAtIndex(length - 1, outTangents);
                CPointF cp2 = vertexAtIndex(0, inTangents);

                CPointF shapeCp1 = MiscUtils.addPoints(previousVertex, cp1);
                CPointF shapeCp2 = MiscUtils.addPoints(vertex, cp2);

                if (scale != 1f) {
                    shapeCp1.scaleX(scale);
                    shapeCp1.scaleY(scale);
                    shapeCp2.scaleX(scale);
                    shapeCp2.scaleY(scale);
                    vertex.scaleX(scale);
                    vertex.scaleY(scale);
                }

                curves.add(new CubicCurveData(shapeCp1, shapeCp2, vertex));
            }
            return new ShapeData(initialPoint, closed, curves);
        }

        private static CPointF vertexAtIndex(int idx, JSONArray points) {
            if (idx >= points.length()) {
                throw new IllegalArgumentException(
                        "Invalid index " + idx + ". There are only " + points.length() + " points.");
            }

            JSONArray pointArray = points.optJSONArray(idx);
            Object x = pointArray.opt(0);
            Object y = pointArray.opt(1);
            return new CPointF(
                    x instanceof Double ? new Float((Double) x) : (int) x,
                    y instanceof Double ? new Float((Double) y) : (int) y);
        }
    }
}
