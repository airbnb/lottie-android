package com.airbnb.lotte.layers;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
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

class LotteEllipseShapeLayer extends LotteAnimatableLayer {

    private final LotteShapeCircle circleShape;
    private final LotteShapeFill fill;
    private final LotteShapeStroke stroke;
    private final LotteShapeTrimPath trim;
    private final LotteShapeTransform transformModel;

    private LotteCircleShapeLayer fillLayer;
    private LotteCircleShapeLayer strokeLayer;

    LotteEllipseShapeLayer(LotteShapeCircle circleShape, LotteShapeFill fill, LotteShapeStroke stroke,
            LotteShapeTrimPath trim, LotteShapeTransform transform, long duration, Drawable.Callback callback) {
        super(duration, callback);
        this.circleShape = circleShape;
        this.fill = fill;
        this.stroke = stroke;
        this.trim = trim;
        this.transformModel = transform;

        setBounds(transform.getCompBounds());
        setAnchorPoint(transform.getAnchor().getObservable());
        setAlpha(transform.getOpacity().getObservable());
        setPosition(transform.getPosition().getObservable());
        setTransform(transform.getScale().getObservable());
        setSublayerTransform(transform.getRotation().getObservable());

        if (fill != null) {
            fillLayer = new LotteCircleShapeLayer(getCallback());
            fillLayer.setColor(fill.getColor().getObservable());
            fillLayer.setAlpha(fill.getOpacity().getObservable());
            fillLayer.updateCircle(
                    circleShape.getPosition().getObservable(),
                    circleShape.getSize().getObservable());
            addLayer(fillLayer);
        }

        if (stroke != null) {
            strokeLayer = new LotteCircleShapeLayer(getCallback());
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
            addAnimation(new LotteAnimationGroup(propertyAnimations, compDuration));
        }

        if (stroke != null && strokeLayer != null) {
            SparseArray<LotteAnimatableValue> propertyAnimations = new SparseArray<>();
            propertyAnimations.put(LotteAnimatableProperty.STROKE_COLOR, stroke.getColor());
            propertyAnimations.put(LotteAnimatableProperty.OPACITY, stroke.getOpacity());
            propertyAnimations.put(LotteAnimatableProperty.LINE_WIDTH, stroke.getWidth());
            propertyAnimations.put(LotteAnimatableProperty.RECT_SIZE, circleShape.getSize());
            propertyAnimations.put(LotteAnimatableProperty.CIRCLE_POSITION, circleShape.getPosition());
            propertyAnimations.put(LotteAnimatableProperty.CIRCLE_SIZE, circleShape.getSize());
            if (!stroke.getLineDashPattern().isEmpty()) {
                propertyAnimations.put(LotteAnimatableProperty.DASH_PATTERN, stroke.getLineDashPattern().get(0));
                propertyAnimations.put(LotteAnimatableProperty.DASH_PATTERN_GAP, stroke.getLineDashPattern().get(1));
                propertyAnimations.put(LotteAnimatableProperty.DASH_PATTERN_OFFSET, stroke.getDashOffset());
            }
            if (trim != null) {
                propertyAnimations.put(LotteAnimatableProperty.TRIM_PATH_START, trim.getStart());
                propertyAnimations.put(LotteAnimatableProperty.TRIM_PATH_END, trim.getEnd());
                propertyAnimations.put(LotteAnimatableProperty.TRIM_PATH_OFFSET, trim.getOffset());
            }
            strokeLayer.addAnimation(new LotteAnimationGroup(propertyAnimations, compDuration));
        }

        if (fill != null && fillLayer != null) {
            SparseArray<LotteAnimatableValue> propertyAnimations = new SparseArray<>();
            propertyAnimations.put(LotteAnimatableProperty.BACKGROUND_COLOR, fill.getColor());
            propertyAnimations.put(LotteAnimatableProperty.OPACITY, fill.getOpacity());
            propertyAnimations.put(LotteAnimatableProperty.RECT_SIZE, circleShape.getSize());
            propertyAnimations.put(LotteAnimatableProperty.CIRCLE_POSITION, circleShape.getPosition());
            propertyAnimations.put(LotteAnimatableProperty.CIRCLE_SIZE, circleShape.getSize());
            fillLayer.addAnimation(new LotteAnimationGroup(propertyAnimations, compDuration));
        }
    }

    private static final class LotteCircleShapeLayer extends LotteShapeLayer {
        private static final float ELLIPSE_CONTROL_POINT_PERCENTAGE = 0.55228f;

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

        private final Paint paint = new Paint();
        private final Path path = new Path();
        private final Observable<Path> observable = new Observable<>(path);

        private Observable<PointF> circleSize;
        private Observable<PointF> circlePosition;

        LotteCircleShapeLayer(Drawable.Callback callback) {
            super(callback);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            setPath(observable);
        }

        void updateCircle(Observable<PointF> circlePosition, Observable<PointF> circleSize) {
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
            setBounds(0, 0, (int) halfWidth * 2, (int) halfHeight * 2);

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
            observable.setValue(path);
            onTrimPathChanged();

            invalidateSelf();
        }
    }
}
