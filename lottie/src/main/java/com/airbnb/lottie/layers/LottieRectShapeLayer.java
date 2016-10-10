package com.airbnb.lottie.layers;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.airbnb.lottie.animation.LottieAnimatableFloatValue;
import com.airbnb.lottie.animation.LottieAnimatableValue;
import com.airbnb.lottie.animation.LottieAnimationGroup;
import com.airbnb.lottie.model.LottieShapeFill;
import com.airbnb.lottie.model.LottieShapeRectangle;
import com.airbnb.lottie.model.LottieShapeStroke;
import com.airbnb.lottie.model.LottieShapeTransform;
import com.airbnb.lottie.utils.Observable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class LottieRectShapeLayer extends LottieAnimatableLayer {

    private final LottieShapeTransform transformModel;
    private final LottieShapeStroke stroke;
    private final LottieShapeFill fill;
    private final LottieShapeRectangle rectShape;

    @Nullable private LottieRoundRectLayer fillLayer;
    @Nullable private LottieRoundRectLayer strokeLayer;

    LottieRectShapeLayer(LottieShapeRectangle rectShape, @Nullable LottieShapeFill fill,
            @Nullable LottieShapeStroke stroke, LottieShapeTransform transform, long duration, Drawable.Callback callback) {
        super(duration, callback);
        this.rectShape = rectShape;
        this.fill = fill;
        this.stroke = stroke;
        this.transformModel = transform;

        setBounds(transform.getCompBounds());
        setAnchorPoint(transform.getAnchor().getObservable());
        setAlpha(transform.getOpacity().getObservable());
        setPosition(transform.getPosition().getObservable());
        setTransform(transform.getScale().getObservable());
        setSublayerTransform(transform.getRotation().getObservable());

        if (fill != null) {
            fillLayer = new LottieRoundRectLayer(duration, getCallback());
            fillLayer.setColor(fill.getColor().getObservable());
            fillLayer.setShapeAlpha(fill.getOpacity().getObservable());
            fillLayer.setTransformAlpha(transformModel.getOpacity().getObservable());
            fillLayer.setRectCornerRadius(rectShape.getCornerRadius().getObservable());
            fillLayer.setRectSize(rectShape.getSize().getObservable());
            fillLayer.setRectPosition(rectShape.getPosition().getObservable());
            addLayer(fillLayer);
        }

        if (stroke != null) {
            strokeLayer = new LottieRoundRectLayer(duration, getCallback());
            strokeLayer.setIsStroke();
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
            Set<LottieAnimatableValue> propertyAnimations = new HashSet<>();
            propertyAnimations.add(transformModel.getOpacity());
            propertyAnimations.add(transformModel.getPosition());
            propertyAnimations.add(transformModel.getAnchor());
            propertyAnimations.add(transformModel.getScale());
            propertyAnimations.add(transformModel.getRotation());
            addAnimation(new LottieAnimationGroup(propertyAnimations));
        }

        if (stroke != null && strokeLayer != null) {
            Set<LottieAnimatableValue> propertyAnimations = new HashSet<>();
            propertyAnimations.add(stroke.getColor());
            propertyAnimations.add(stroke.getOpacity());
            propertyAnimations.add(stroke.getWidth());
            propertyAnimations.add(rectShape.getSize());
            propertyAnimations.add(rectShape.getPosition());
            propertyAnimations.add(rectShape.getCornerRadius());
            if (!stroke.getLineDashPattern().isEmpty()) {
                propertyAnimations.add(stroke.getLineDashPattern().get(0));
                propertyAnimations.add(stroke.getLineDashPattern().get(1));
                propertyAnimations.add(stroke.getDashOffset());
            }
            strokeLayer.addAnimation(new LottieAnimationGroup(propertyAnimations));
        }

        if (fill != null && fillLayer != null) {
            Set<LottieAnimatableValue> propertyAnimations = new HashSet<>();
            propertyAnimations.add(fill.getColor());
            propertyAnimations.add(fill.getOpacity());
            propertyAnimations.add(rectShape.getSize());
            propertyAnimations.add(rectShape.getPosition());
            propertyAnimations.add(rectShape.getCornerRadius());
            fillLayer.addAnimation(new LottieAnimationGroup(propertyAnimations));
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

    private static class LottieRoundRectLayer extends LottieAnimatableLayer {

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

        private final Observable.OnChangedListener dashPatternChangedListener = new Observable.OnChangedListener() {
            @Override
            public void onChanged() {
                onDashPatternChanged();
            }
        };

        private final Paint paint = new Paint();
        private final RectF fillRect = new RectF();

        private Observable<Integer> color;
        private Observable<Float> lineWidth;
        private Observable<Integer> shapeAlpha;
        private Observable<Integer> transformAlpha;
        private Observable<Float> rectCornerRadius;
        private Observable<PointF> rectPosition;
        private Observable<PointF> rectSize;

        @Nullable private List<LottieAnimatableFloatValue> lineDashPattern;
        @Nullable private LottieAnimatableFloatValue lineDashPatternOffset;

        LottieRoundRectLayer(long duration, Drawable.Callback callback) {
            super(duration, callback);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
        }

        void setShapeAlpha(Observable<Integer> alpha) {
            if (this.shapeAlpha != null) {
                this.shapeAlpha.removeChangeListener(alphaChangedListener);
            }
            this.shapeAlpha = alpha;
            alpha.addChangeListener(alphaChangedListener);
            onAlphaChanged();
        }

        void setTransformAlpha(Observable<Integer> alpha) {
            if (this.transformAlpha != null) {
                this.transformAlpha.removeChangeListener(alphaChangedListener);
            }
            transformAlpha = alpha;
            alpha.addChangeListener(alphaChangedListener);
            onAlphaChanged();
        }

        private void onAlphaChanged() {
            Integer shapeAlpha = this.shapeAlpha == null ? 255 : this.shapeAlpha.getValue();
            Integer transformAlpha = this.transformAlpha == null ? 255 : this.transformAlpha.getValue();
            setAlpha((int) ((shapeAlpha / 255f * transformAlpha / 255f) * 255));
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
                this.color.removeChangeListener(colorChangedListener);
            }
            this.color = color;
            color.addChangeListener(colorChangedListener);
            onColorChanged();
        }

        private void onColorChanged() {
            paint.setColor(color.getValue());
            invalidateSelf();
        }

        private void setIsStroke() {
            paint.setStyle(Paint.Style.STROKE);
            invalidateSelf();
        }

        void setLineWidth(Observable<Float> lineWidth) {
            if (this.lineWidth != null) {
                this.lineWidth.removeChangeListener(lineWidthChangedListener);
            }
            this.lineWidth = lineWidth;
            lineWidth.addChangeListener(lineWidthChangedListener);
            onLineWidthChanged();
        }

        private void onLineWidthChanged() {
            paint.setStrokeWidth(lineWidth.getValue());
            invalidateSelf();
        }

        void setDashPattern(List<LottieAnimatableFloatValue> lineDashPattern, LottieAnimatableFloatValue offset) {
            if (this.lineDashPattern != null) {
                this.lineDashPattern.get(0).getObservable().removeChangeListener(dashPatternChangedListener);
                this.lineDashPattern.get(1).getObservable().removeChangeListener(dashPatternChangedListener);
            }
            if (this.lineDashPatternOffset != null) {
                this.lineDashPatternOffset.getObservable().removeChangeListener(dashPatternChangedListener);
            }
            if (lineDashPattern.isEmpty()) {
                return;
            }
            this.lineDashPattern = lineDashPattern;
            this.lineDashPatternOffset = offset;
            lineDashPattern.get(0).getObservable().addChangeListener(dashPatternChangedListener);
            if (!lineDashPattern.get(1).equals(lineDashPattern.get(1))) {
                lineDashPattern.get(1).getObservable().addChangeListener(dashPatternChangedListener);
            }
            offset.getObservable().addChangeListener(dashPatternChangedListener);
            onDashPatternChanged();
        }

        private void onDashPatternChanged() {
            if (lineDashPattern == null || lineDashPatternOffset == null) {
                throw new IllegalStateException("LineDashPattern is null");
            }
            float[] values = new float[lineDashPattern.size()];
            for (int i = 0; i < lineDashPattern.size(); i++) {
                values[i] = lineDashPattern.get(i).getObservable().getValue();
            }
            paint.setPathEffect(new DashPathEffect(values, lineDashPatternOffset.getObservable().getValue()));
            invalidateSelf();
        }

        void setLineCapType(LottieShapeStroke.LineCapType lineCapType) {
            switch (lineCapType) {
                case Round:
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    break;
                case Butt:
                    paint.setStrokeCap(Paint.Cap.BUTT);
                default:
            }
        }

        void setLineJoinType(LottieShapeStroke.LineJoinType lineJoinType) {
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

        void setRectCornerRadius(Observable<Float> rectCornerRadius) {
            if (this.rectCornerRadius != null) {
                this.rectCornerRadius.removeChangeListener(changedListener);
            }
            this.rectCornerRadius = rectCornerRadius;
            rectCornerRadius.addChangeListener(changedListener);
            invalidateSelf();
        }

        void setRectPosition(Observable<PointF> rectPosition) {
            if (this.rectPosition != null) {
                this.rectPosition.removeChangeListener(changedListener);
            }
            this.rectPosition = rectPosition;
            rectPosition.addChangeListener(changedListener);
            invalidateSelf();
        }

        void setRectSize(Observable<PointF> rectSize) {
            if (this.rectSize != null) {
                this.rectSize.removeChangeListener(changedListener);
            }
            this.rectSize = rectSize;
            this.rectSize.addChangeListener(changedListener);
            invalidateSelf();
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            if (paint.getStyle() == Paint.Style.STROKE && paint.getStrokeWidth() == 0f) {
                return;
            }
            super.draw(canvas);
            float halfWidth = rectSize.getValue().x / 2f;
            float halfHeight = rectSize.getValue().y / 2f;

            fillRect.set(rectPosition.getValue().x - halfWidth,
                    rectPosition.getValue().y - halfHeight,
                    rectPosition.getValue().x + halfWidth,
                    rectPosition.getValue().y + halfHeight);
            if (rectCornerRadius.getValue() == 0) {
                canvas.drawRect(fillRect, paint);
            } else {
                canvas.drawRoundRect(fillRect, rectCornerRadius.getValue(), rectCornerRadius.getValue(), paint);
            }
        }
    }

}
