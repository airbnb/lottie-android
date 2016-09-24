package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.airbnb.lotte.animation.LotteAnimatableProperty;
import com.airbnb.lotte.animation.LotteAnimatableValue;
import com.airbnb.lotte.animation.LotteAnimationGroup;
import com.airbnb.lotte.model.LotteShapeFill;
import com.airbnb.lotte.model.LotteShapeRectangle;
import com.airbnb.lotte.model.LotteShapeStroke;
import com.airbnb.lotte.model.LotteShapeTransform;
import com.airbnb.lotte.utils.Observable;

import java.util.List;

public class LotteRectShapeLayer extends LotteAnimatableLayer {

    private final Paint paint = new Paint();

    private final LotteShapeTransform transformModel;
    private final LotteShapeStroke stroke;
    private final LotteShapeFill fill;
    private final LotteShapeRectangle rectShape;

    @Nullable private LotteRoundRectLayer fillLayer;
    @Nullable private LotteRoundRectLayer strokeLayer;

    public LotteRectShapeLayer(LotteShapeRectangle rectShape, @Nullable LotteShapeFill fill,
            @Nullable LotteShapeStroke stroke, LotteShapeTransform transform, long duration, Drawable.Callback callback) {
        super(duration, callback);
        this.rectShape = rectShape;
        this.fill = fill;
        this.stroke = stroke;
        this.transformModel = transform;

        paint.setAntiAlias(true);
        setBounds(transform.getCompBounds());
        anchorPoint = transform.getAnchor().getObservable();
        setAlpha(transform.getOpacity().getObservable());
        setPosition(transform.getPosition().getObservable());
        setTransform(transform.getScale().getObservable());
        sublayerTransform = transform.getRotation().getObservable();

        if (fill != null) {
            fillLayer = new LotteRoundRectLayer(duration, getCallback());
            fillLayer.setColor(fill.getColor().getObservable());
            fillLayer.setShapeAlpha(fill.getOpacity().getObservable());
            fillLayer.setTransformAlpha(transformModel.getOpacity().getObservable());
            fillLayer.setRectCornerRadius(rectShape.getCornerRadius().getObservable());
            fillLayer.setRectSize(rectShape.getSize().getObservable());
            fillLayer.setRectPosition(rectShape.getPosition().getObservable());
            addLayer(fillLayer);
        }

        if (stroke != null) {
            strokeLayer = new LotteRoundRectLayer(duration, getCallback());
            strokeLayer.setStyle(Paint.Style.STROKE);
            strokeLayer.setColor(stroke.getColor().getObservable());
            strokeLayer.setShapeAlpha(stroke.getOpacity().getObservable());
            strokeLayer.setTransformAlpha(transformModel.getOpacity().getObservable());
            strokeLayer.setLineWidth(stroke.getWidth().getObservable());
            strokeLayer.setDashPattern(stroke.getLineDashPattern(), stroke.getDashOffset());
            strokeLayer.setLineCapType(stroke.getCapType());
            strokeLayer.setRectCornerRadius(rectShape.getCornerRadius().getObservable());
            strokeLayer.setRectSize(rectShape.getSize().getObservable());
            strokeLayer.setRectPosition(rectShape.getPosition().getObservable());
            strokeLayer.setLineJoinType(stroke.getJoinType());
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
            propertyAnimations.put(LotteAnimatableProperty.RECT_SIZE, rectShape.getSize());
            propertyAnimations.put(LotteAnimatableProperty.RECT_POSITION, rectShape.getPosition());
            propertyAnimations.put(LotteAnimatableProperty.RECT_CORNER_RADIUS, rectShape.getCornerRadius());
            strokeLayer.addAnimation(new LotteAnimationGroup(propertyAnimations));
        }

        if (fill != null && fillLayer != null) {
            SparseArray<LotteAnimatableValue> propertyAnimations = new SparseArray<>();
            propertyAnimations.put(LotteAnimatableProperty.BACKGROUND_COLOR, fill.getColor());
            propertyAnimations.put(LotteAnimatableProperty.OPACITY, fill.getOpacity());
            propertyAnimations.put(LotteAnimatableProperty.RECT_SIZE, rectShape.getSize());
            propertyAnimations.put(LotteAnimatableProperty.RECT_POSITION, rectShape.getPosition());
            propertyAnimations.put(LotteAnimatableProperty.RECT_CORNER_RADIUS, rectShape.getCornerRadius());
            fillLayer.addAnimation(new LotteAnimationGroup(propertyAnimations));
        }
    }

    @Override
    public void setAlpha(int alpha) {
        super.setAlpha(alpha);
        if (fillLayer != null) {
            fillLayer.setAlpha(alpha);
        }
        if (strokeLayer != null) {
            strokeLayer.setAlpha(alpha);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
    }

    private static class LotteRoundRectLayer extends LotteAnimatableLayer {
        private static final String TAG = LotteRoundRectLayer.class.getSimpleName();

        private final Observable.OnChangedListener changedListener = new Observable.OnChangedListener() {
            @Override
            public void onChanged() {
                invalidateSelf();
            }
        };

        private final Observable.OnChangedListener alphaChangedListener = new Observable.OnChangedListener() {
            @Override
            public void onChanged() {
                onAlphaChanged();
            }
        };

        private final Observable.OnChangedListener colorChangedListener = new Observable.OnChangedListener() {
            @Override
            public void onChanged() {
                onColorChanged();
            }
        };

        private final Observable.OnChangedListener lineWidthChangedListener = new Observable.OnChangedListener() {
            @Override
            public void onChanged() {
                onLineWidthChanged();
            }
        };

        private final Paint paint = new Paint();
        private final RectF fillRect = new RectF();

        private Observable<Integer> color;
        private Observable<Number> lineWidth;
        private Observable<Number> shapeAlpha;
        private Observable<Number> transformAlpha;
        private Observable<Number> rectCornerRadius;
        private Observable<PointF> rectPosition;
        private Observable<PointF> rectSize;

        @Nullable private PathEffect dashPatternPathEffect;
        @Nullable private PathEffect lineJoinPathEffect;

        LotteRoundRectLayer(long duration, Drawable.Callback callback) {
            super(duration, callback);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
        }

        public void setShapeAlpha(Observable<Number> alpha) {
            if (this.shapeAlpha != null) {
                this.shapeAlpha.removeChangeListemer(alphaChangedListener);
            }
            this.shapeAlpha = alpha;
            alpha.addChangeListener(alphaChangedListener);
            onAlphaChanged();
        }

        public void setTransformAlpha(Observable<Number> alpha) {
            if (this.transformAlpha != null) {
                this.transformAlpha.removeChangeListemer(alphaChangedListener);
            }
            transformAlpha = alpha;
            alpha.addChangeListener(alphaChangedListener);
            onAlphaChanged();
        }

        private void onAlphaChanged() {
            Float shapeAlpha = this.shapeAlpha == null ? 1f : (Float) this.shapeAlpha.getValue();
            Float transformAlpha = this.transformAlpha == null ? 1f : (Float) this.transformAlpha.getValue();
            setAlpha((int) ((shapeAlpha * transformAlpha) * 255));
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public int getAlpha() {
            return paint.getAlpha();
        }

        public void setColor(Observable<Integer> color) {
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
            invalidateSelf();
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
                    paint.setStrokeCap(Paint.Cap.BUTT);
                default:
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

        public void setRectCornerRadius(Observable<Number> rectCornerRadius) {
            if (this.rectCornerRadius != null) {
                this.rectCornerRadius.removeChangeListemer(changedListener);
            }
            this.rectCornerRadius = rectCornerRadius;
            rectCornerRadius.addChangeListener(changedListener);
            invalidateSelf();
        }

        public void setRectPosition(Observable<PointF> rectPosition) {
            if (this.rectPosition != null) {
                this.rectPosition.removeChangeListemer(changedListener);
            }
            this.rectPosition = rectPosition;
            rectPosition.addChangeListener(changedListener);
            invalidateSelf();
        }

        public void setRectSize(Observable<PointF> rectSize) {
            if (this.rectSize != null) {
                this.rectSize.removeChangeListemer(changedListener);
            }
            this.rectSize = rectSize;
            this.rectSize.addChangeListener(changedListener);
            invalidateSelf();
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            super.draw(canvas);
            float halfWidth = rectSize.getValue().x / 2f;
            float halfHeight = rectSize.getValue().y / 2f;

            fillRect.set(rectPosition.getValue().x - halfWidth,
                    rectPosition.getValue().y - halfHeight,
                    rectPosition.getValue().x + halfWidth,
                    rectPosition.getValue().y + halfHeight);
            if ((float) rectCornerRadius.getValue() == 0) {
                canvas.drawRect(fillRect, paint);
            } else {
                canvas.drawRoundRect(fillRect, (float) rectCornerRadius.getValue(), (float) rectCornerRadius.getValue(), paint);
            }
        }
    }

}
