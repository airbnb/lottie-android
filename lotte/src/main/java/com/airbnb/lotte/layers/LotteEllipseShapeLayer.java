package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.airbnb.lotte.animation.LotteAnimatableProperty;
import com.airbnb.lotte.animation.LotteAnimatableValue;
import com.airbnb.lotte.animation.LotteAnimationGroup;
import com.airbnb.lotte.model.LotteShapeCircle;
import com.airbnb.lotte.model.LotteShapeFill;
import com.airbnb.lotte.model.LotteShapeStroke;
import com.airbnb.lotte.model.LotteShapeTransform;
import com.airbnb.lotte.model.LotteShapeTrimPath;
import com.airbnb.lotte.utils.Observable;

import java.util.List;

public class LotteEllipseShapeLayer extends LotteAnimatableLayer {

    private final LotteShapeCircle circleShape;
    private final LotteShapeFill fill;
    private final LotteShapeStroke stroke;
    private final LotteShapeTrimPath trim;
    private final LotteShapeTransform transformModel;

    private LotteCircleShapeLayer fillLayer;
    private LotteCircleShapeLayer strokeLayer;

    private LotteAnimationGroup animation;
    private LotteAnimationGroup strokeAnimation;
    private LotteAnimationGroup fillAnimation;

    public LotteEllipseShapeLayer(LotteShapeCircle circleShape, LotteShapeFill fill, LotteShapeStroke stroke,
            LotteShapeTrimPath trim, LotteShapeTransform transform, long duration, Drawable.Callback callback) {
        super(duration, callback);
        this.circleShape = circleShape;
        this.fill = fill;
        this.stroke = stroke;
        this.trim = trim;
        this.transformModel = transform;

        setBounds(transform.getCompBounds());
        anchorPoint = transform.getAnchor().getObservable();
        setAlpha(transform.getOpacity().getObservable());
        setPosition(transform.getPosition().getObservable());
        setTransform(transform.getScale().getObservable());
        setSublayerTransform(transform.getRotation().getObservable());

        if (fill != null) {
            fillLayer = new LotteCircleShapeLayer(duration, getCallback());
            fillLayer.setColor(fill.getColor().getObservable());
            fillLayer.setAlpha(fill.getOpacity().getObservable());
            fillLayer.updateCircle(
                    circleShape.getPosition().getObservable(),
                    circleShape.getSize().getObservable());
            addLayer(fillLayer);
        }

        if (stroke != null) {
            strokeLayer = new LotteCircleShapeLayer(duration, getCallback());
            strokeLayer.setStyle(Paint.Style.STROKE);
            strokeLayer.setColor(stroke.getColor().getObservable());
            strokeLayer.setAlpha(stroke.getOpacity().getObservable());
            strokeLayer.setLineWidth(stroke.getWidth().getObservable());
            strokeLayer.setDashPattern(stroke.getLineDashPattern(), stroke.getDashOffset());
            strokeLayer.setLineCapType(stroke.getCapType());
            strokeLayer.updateCircle(
                    circleShape.getPosition().getObservable(),
                    circleShape.getSize().getObservable());
            if (trim != null) {
                strokeLayer.setTrimPath(trim.getStart().getObservable(), trim.getEnd().getObservable());
            }

            addLayer(strokeLayer);
        }

        buildAnimation();
    }

    private void buildAnimation() {
        if (transformModel != null) {
            SparseArray<LotteAnimatableValue> propertyAnimations = new SparseArray<>();
            propertyAnimations.put(LotteAnimatableProperty.OPACITY, transformModel.getOpacity());
            propertyAnimations.put(LotteAnimatableProperty.POSITION, transformModel.getPosition());
            propertyAnimations.put(LotteAnimatableProperty.ANCHOR_POINT, transformModel.getAnchor());
            propertyAnimations.put(LotteAnimatableProperty.TRANSFORM, transformModel.getScale());
            propertyAnimations.put(LotteAnimatableProperty.SUBLAYER_TRANSFORM, transformModel.getRotation());
            addAnimation(new LotteAnimationGroup(propertyAnimations));
        }

        if (stroke != null && strokeLayer != null) {
            SparseArray<LotteAnimatableValue> propertyAnimations = new SparseArray<>();
            propertyAnimations.put(LotteAnimatableProperty.STROKE_COLOR, stroke.getColor());
            propertyAnimations.put(LotteAnimatableProperty.OPACITY, stroke.getOpacity());
            propertyAnimations.put(LotteAnimatableProperty.LINE_WIDTH, stroke.getWidth());
            propertyAnimations.put(LotteAnimatableProperty.RECT_SIZE, circleShape.getSize());
            propertyAnimations.put(LotteAnimatableProperty.CIRCLE_POSITION, circleShape.getPosition());
            propertyAnimations.put(LotteAnimatableProperty.CIRCLE_SIZE, circleShape.getSize());
            strokeLayer.addAnimation(new LotteAnimationGroup(propertyAnimations));
        }

        if (fill != null && fillLayer != null) {
            SparseArray<LotteAnimatableValue> propertyAnimations = new SparseArray<>();
            propertyAnimations.put(LotteAnimatableProperty.BACKGROUND_COLOR, fill.getColor());
            propertyAnimations.put(LotteAnimatableProperty.OPACITY, fill.getOpacity());
            propertyAnimations.put(LotteAnimatableProperty.RECT_SIZE, circleShape.getSize());
            propertyAnimations.put(LotteAnimatableProperty.CIRCLE_POSITION, circleShape.getPosition());
            propertyAnimations.put(LotteAnimatableProperty.CIRCLE_SIZE, circleShape.getSize());
            fillLayer.addAnimation(new LotteAnimationGroup(propertyAnimations));
        }
    }

    private static final class LotteCircleShapeLayer extends LotteAnimatableLayer {
        private static final float ELLIPSE_CONTROL_POINT_PERCENTAGE = 0.55228f;

        private final Observable.OnChangedListener lineWidthChangedListener = new Observable.OnChangedListener() {
            @Override
            public void onChanged() {
                onLineWidthChanged();
            }
        };

        private final Observable.OnChangedListener colorChangedListener = new Observable.OnChangedListener() {
            @Override
            public void onChanged() {
                onColorChanged();
            }
        };

        private final Observable.OnChangedListener circleSizeChangedListener = new Observable.OnChangedListener() {
            @Override
            public void onChanged() {
                onCircleSizeChanged();
            }
        };

        private final Observable.OnChangedListener circlePositionListener = new Observable.OnChangedListener() {
            @Override
            public void onChanged() {
                invalidateSelf();
            }
        };

        private final RectF rect = new RectF();
        private final Paint paint = new Paint();
        private final Path path = new Path();
        private final Path trimPath = new Path();
        private final PathMeasure pathMeasure = new PathMeasure();

        private Observable<PointF> circleSize;
        private Observable<PointF> circlePosition;
        @Nullable private Observable<Number> lineWidth;
        @Nullable private Observable<Number> strokeStart;
        @Nullable private Observable<Number> strokeEnd;
        @Nullable private Observable<Integer> color;

        public LotteCircleShapeLayer(long duration, Drawable.Callback callback) {
            super(duration, callback);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
        }

        public void updateCircle(Observable<PointF> circlePosition, Observable<PointF> circleSize) {
            if (this.circleSize != null) {
                this.circleSize.removeChangeListemer(circleSizeChangedListener);
            }
            if (this.circlePosition != null)
                this.circlePosition.removeChangeListemer(circlePositionListener);
            this.circleSize = circleSize;
            this.circlePosition = circlePosition;
            circleSize.addChangeListener(circleSizeChangedListener);
            circlePosition.addChangeListener(circlePositionListener);
            onCircleSizeChanged();
        }

        private void onCircleSizeChanged() {
            float halfWidth = circleSize.getValue().x / 2f;
            float halfHeight = circleSize.getValue().y / 2f;

            PointF circleQ1 = new PointF(0, -halfHeight);
            PointF circleQ2 = new PointF(halfWidth, 0);
            PointF circleQ3 = new PointF(0, halfHeight);
            PointF circleQ4 = new PointF(-halfWidth, 0);

            float cpW = halfWidth * ELLIPSE_CONTROL_POINT_PERCENTAGE;
            float cpH = halfHeight * ELLIPSE_CONTROL_POINT_PERCENTAGE;

            path.reset();
            path.moveTo(circleQ1.x, circleQ1.y);
            path.cubicTo(circleQ1.x + cpW, circleQ1.y, circleQ2.x, circleQ2.y - cpH, circleQ2.x, circleQ2.y);
            path.cubicTo(circleQ2.x, circleQ2.y + cpH, circleQ3.x + cpW, circleQ3.y, circleQ3.x, circleQ3.y);
            path.cubicTo(circleQ3.x - cpW, circleQ3.y, circleQ4.x, circleQ4.y + cpH, circleQ4.x, circleQ4.y);
            path.cubicTo(circleQ4.x, circleQ4.y - cpH, circleQ1.x - cpW, circleQ1.y, circleQ1.x, circleQ1.y);

            pathMeasure.setPath(path, false);
            updateTrimPath();
            invalidateSelf();
        }

        public void setTrimPath(Observable<Number> strokeStart, Observable<Number> strokeEnd) {
            if (this.strokeStart != null) {
                this.strokeStart.removeChangeListemer(changedListener);
            }
            if (this.strokeEnd != null) {
                this.strokeEnd.removeChangeListemer(changedListener);
            }

            this.strokeStart = strokeStart;
            this.strokeEnd = strokeEnd;

            strokeStart.addChangeListener(changedListener);
            strokeEnd.addChangeListener(changedListener);
            updateTrimPath();
            invalidateSelf();
        }

        private void updateTrimPath() {
            if (strokeStart != null && strokeEnd != null) {
                float length = pathMeasure.getLength();
                float start = length * ((Float) strokeStart.getValue()) / 100f;
                float end = length * ((Float) strokeEnd.getValue()) / 100f;

                pathMeasure.getSegment(
                        Math.min(start, end),
                        Math.max(start, end),
                        trimPath,
                        true);
            }
        }

        public void setStyle(Paint.Style style) {
            paint.setStyle(style);
        }

        public void setLineWidth(Observable<Number> lineWidth) {
            if (this.lineWidth != null) {
                this.lineWidth.removeChangeListemer(lineWidthChangedListener);
            }
            this.lineWidth = lineWidth;
            lineWidth.addChangeListener(lineWidthChangedListener);
            onLineWidthChanged();
        }

        private void onLineWidthChanged() {
            paint.setStrokeWidth((float) lineWidth.getValue());
        }

        public void setDashPattern(List<Float> lineDashPattern, float offset) {
            if (lineDashPattern.isEmpty()) {
                return;
            }
            float[] values = new float[lineDashPattern.size()];
            for (int i = 0; i < lineDashPattern.size(); i++) {
                values[i] = lineDashPattern.get(i);
            }
            paint.setPathEffect(new DashPathEffect(values, offset));
        }

        public void setLineCapType(LotteShapeStroke.LineCapType lineCapType) {
            switch (lineCapType) {
                case Round:
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    break;
                case Butt:
                default:
                    paint.setStrokeCap(Paint.Cap.BUTT);
            }
        }

        public void setLineJoinType(LotteShapeStroke.LineJoinType lineJoinType) {
            switch (lineJoinType) {
                case Bevel:
                    paint.setStrokeJoin(Paint.Join.BEVEL);
                    break;
                case Miter:
                    paint.setStrokeJoin(Paint.Join.MITER);
                    break;
                case Round:
                    paint.setStrokeJoin(Paint.Join.ROUND);
                    break;
            }
        }

        void setColor(Observable<Integer> color) {
            if (this.color != null) {
                this.color.removeChangeListemer(colorChangedListener);
            }
            this.color = color;
            color.addChangeListener(colorChangedListener);
            onColorChanged();
        }

        private void onColorChanged() {
            paint.setColor(color.getValue());
            invalidateSelf();
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            super.draw(canvas);
            if (trimPath.isEmpty()) {
                canvas.drawPath(path, paint);
            } else {
                canvas.drawPath(trimPath, paint);
            }
        }
    }
}
