package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.airbnb.lotte.L;
import com.airbnb.lotte.model.LotteShapeFill;
import com.airbnb.lotte.model.LotteShapeRectangle;
import com.airbnb.lotte.model.LotteShapeStroke;
import com.airbnb.lotte.model.LotteShapeTransform;
import com.airbnb.lotte.utils.LotteAnimationGroup;
import com.airbnb.lotte.utils.LotteTransform3D;

import java.util.List;

public class LotteRectShapeLayer extends LotteAnimatableLayer {

    private final Paint paint = new Paint();

    private final LotteShapeTransform transformModel;
    private final LotteShapeStroke stroke;
    private final LotteShapeFill fill;
    private final LotteShapeRectangle rectShape;

    @Nullable private LotteRoundRectLayer fillLayer;
    @Nullable private LotteRoundRectLayer strokeLayer;

    LotteAnimationGroup animation;
    LotteAnimationGroup strokeAnimation;
    LotteAnimationGroup fillAanimation;

    public LotteRectShapeLayer(LotteShapeRectangle rectShape, @Nullable LotteShapeFill fill,
            @Nullable LotteShapeStroke stroke, LotteShapeTransform transform, long duration) {
        super(duration);
        this.rectShape = rectShape;
        this.fill = fill;
        this.stroke = stroke;
        this.transformModel = transform;

        paint.setAntiAlias(true);
        setBounds(transform.getCompBounds());
        anchorPoint = transform.getAnchor().getInitialPoint();
        setAlpha((int) (transform.getOpacity().getInitialValue()));
        position = transform.getPosition().getInitialPoint();
        this.transform = transform.getScale().getInitialScale();
        sublayerTransform = new LotteTransform3D();
        sublayerTransform.rotateZ(transform.getRotation().getInitialValue());

        if (fill != null) {
            fillLayer = new LotteRoundRectLayer(duration);
            fillLayer.setColor(fill.getColor().getInitialColor());
            fillLayer.setShapeAlpha((int) (fill.getOpacity().getInitialValue()));
            fillLayer.setTransformAlpha((int) transformModel.getOpacity().getInitialValue());
            fillLayer.setRectCornerRadius(rectShape.getCornerRadius().getInitialValue());
            fillLayer.setRectSize(rectShape.getSize().getInitialPoint());
            fillLayer.setRectPosition(rectShape.getPosition().getInitialPoint());
            addLayer(fillLayer);
        }

        if (stroke != null) {
            strokeLayer = new LotteRoundRectLayer(duration);
            strokeLayer.setStyle(Paint.Style.STROKE);
            strokeLayer.setColor(stroke.getColor().getInitialColor());
            strokeLayer.setShapeAlpha((int) (stroke.getOpacity().getInitialValue()));
            strokeLayer.setTransformAlpha((int) transformModel.getOpacity().getInitialValue());
            strokeLayer.setLineWidth(stroke.getWidth().getInitialValue());
            strokeLayer.setDashPattern(stroke.getLineDashPattern(), stroke.getDashOffset());
            strokeLayer.setLineCapType(stroke.getCapType());
            strokeLayer.setRectCornerRadius(rectShape.getCornerRadius().getInitialValue());
            strokeLayer.setRectSize(rectShape.getSize().getInitialPoint());
            strokeLayer.setRectPosition(rectShape.getPosition().getInitialPoint());
            strokeLayer.setLineJoinType(stroke.getJoinType());
            addLayer(strokeLayer);
        }

        // TODO
    }

    private void buildAnimation() {
        // TODO
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

        private final Paint paint = new Paint();
        private final RectF fillRect = new RectF();

        private PointF rectPosition;
        private PointF rectSize;
        private float rectCornerRadius;

        @IntRange(from = 0, to = 255) private int shapeAlpha;
        @IntRange(from = 0, to = 255) private int transformAlpha;

        @Nullable private PathEffect dashPatternPathEffect;
        @Nullable private PathEffect lineJoinPathEffect;

        LotteRoundRectLayer(long duration) {
            super(duration);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
        }

        public void setShapeAlpha(@IntRange(from = 0, to = 255) int alpha) {
            this.shapeAlpha = alpha;
            setAlpha((shapeAlpha * transformAlpha) / 255);
        }

        public void setTransformAlpha(@IntRange(from = 0, to = 255) int alpha) {
            transformAlpha = alpha;
            setAlpha((shapeAlpha * transformAlpha) / 255);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public int getAlpha() {
            return paint.getAlpha();
        }

        public void setColor(@ColorInt int color) {
            paint.setColor(color);
        }

        public void setStyle(Paint.Style style) {
            paint.setStyle(style);
        }

        public void setLineWidth(float width) {
            paint.setStrokeWidth(width);
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

        public float getRectCornerRadius() {
            return rectCornerRadius;
        }

        public void setRectCornerRadius(float rectCornerRadius) {
            this.rectCornerRadius = rectCornerRadius;
        }

        public PointF getRectPosition() {
            return rectPosition;
        }

        public void setRectPosition(PointF rectPosition) {
            this.rectPosition = rectPosition;
        }

        public PointF getRectSize() {
            return rectSize;
        }

        public void setRectSize(PointF rectSize) {
            this.rectSize = rectSize;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            super.draw(canvas);
            float halfWidth = rectSize.x / 2f;
            float halfHeight = rectSize.y / 2f;

            fillRect.set(rectPosition.x - halfWidth,
                    rectPosition.y - halfHeight,
                    rectPosition.x + halfWidth,
                    rectPosition.y + halfHeight);
            if (L.DBG) Log.d(TAG, "Drawing round rect " + fillRect.toShortString() + " radius " + rectCornerRadius);
            if (rectCornerRadius == 0) {
                canvas.drawRect(fillRect, paint);
            } else {
                canvas.drawRoundRect(fillRect, rectCornerRadius, rectCornerRadius, paint);
            }
        }
    }

}
