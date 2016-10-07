package com.airbnb.lotte.model;

import android.graphics.PointF;
import android.support.annotation.FloatRange;

import java.util.ArrayList;
import java.util.List;

import static com.airbnb.lotte.utils.MiscUtils.lerp;

public class LotteShapeData {

    private final List<LotteCubicCurveData> curves = new ArrayList<>();
    private PointF initialPoint;

    public void setInitialPoint(PointF initialPoint) {
        this.initialPoint = initialPoint;
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

    public void addCurve(LotteCubicCurveData curve) {
        curves.add(curve);
    }

    public List<LotteCubicCurveData> getCurves() {
        return curves;
    }

    public void interpolateBetween(LotteShapeData shapeData1, LotteShapeData shapeData2, @FloatRange(from=0f, to=1f) float percentage) {
        if (initialPoint == null) {
            initialPoint = new PointF();
        }
        if (!curves.isEmpty() && curves.size() != shapeData1.getCurves().size() && curves.size() != shapeData2.getCurves().size()) {
            throw new IllegalStateException("Curves must have the same number of control points. This: " + getCurves().size() +
                    "\tShape 1: " + shapeData1.getCurves().size() + "\tShape 2: " + shapeData2.getCurves().size());
        } else if (curves.isEmpty()) {
            for (int i = shapeData1.getCurves().size() - 1; i >= 0; i--) {
                curves.add(new LotteCubicCurveData());
            }
        }

        PointF initialPoint1 = shapeData1.getInitialPoint();
        PointF initialPoint2 = shapeData2.getInitialPoint();

        setInitialPoint(lerp(initialPoint1.x, initialPoint2.x, percentage), lerp(initialPoint1.y, initialPoint2.y, percentage));

        for (int i = curves.size() - 1; i >= 0 ; i--) {
            LotteCubicCurveData curve1 = shapeData1.getCurves().get(i);
            LotteCubicCurveData curve2 = shapeData2.getCurves().get(i);

            PointF cp11 = curve1.getControlPoint1();
            PointF cp21 = curve1.getControlPoint2();
            PointF vertex1 = curve1.getVertex();

            PointF cp12 = curve2.getControlPoint1();
            PointF cp22 = curve2.getControlPoint2();
            PointF vertex2 = curve2.getVertex();

            curves.get(i).setControlPoint1(lerp(cp11.x, cp12.x, percentage), lerp(cp11.y, cp12.y, percentage));
            curves.get(i).setControlPoint2(lerp(cp21.x, cp22.x, percentage), lerp(cp21.y, cp22.y, percentage));
            curves.get(i).setVertex(lerp(vertex1.x, vertex2.x, percentage), lerp(vertex1.y, vertex2.y, percentage));
        }
    }
}
