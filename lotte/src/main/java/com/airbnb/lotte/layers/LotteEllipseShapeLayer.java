package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.airbnb.lotte.model.LotteShapeCircle;
import com.airbnb.lotte.model.LotteShapeFill;
import com.airbnb.lotte.model.LotteShapeStroke;
import com.airbnb.lotte.model.LotteShapeTransform;
import com.airbnb.lotte.model.LotteShapeTrimPath;
import com.airbnb.lotte.utils.LotteAnimationGroup;
import com.airbnb.lotte.utils.LotteTransform3D;

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
            LotteShapeTrimPath trim, LotteShapeTransform transform, long duration) {
        super(duration);
        this.circleShape = circleShape;
        this.fill = fill;
        this.stroke = stroke;
        this.trim = trim;
        this.transformModel = transform;

        setBounds(transform.getCompBounds());
        anchorPoint = transform.getAnchor().getInitialPoint();
        setAlpha((int) (transform.getOpacity().getInitialValue() * 255));
        position = transform.getPosition().getInitialPoint();
        this.transform = transform.getScale().getInitialScale();
        sublayerTransform = new LotteTransform3D();
        sublayerTransform.rotateZ((float) Math.toDegrees(transform.getRotation().getInitialValue()));

        if (fill != null) {
            fillLayer = new LotteCircleShapeLayer(duration);
            fillLayer.setColor(fill.getColor().getInitialColor());
            fillLayer.setAlpha((int) (fill.getOpacity().getInitialValue() * 255));
            fillLayer.updateCircle(
                    circleShape.getPosition().getInitialPoint(),
                    circleShape.getSize().getInitialPoint());
            addLayer(fillLayer);
        }

        if (stroke != null) {
            strokeLayer = new LotteCircleShapeLayer(duration);
            strokeLayer.setStyle(Paint.Style.STROKE);
            strokeLayer.setColor(stroke.getColor().getInitialColor());
            strokeLayer.setAlpha((int) (stroke.getOpacity().getInitialValue() * 255));
            strokeLayer.setLineWidth(stroke.getWidth().getInitialValue());
            strokeLayer.setDashPattern(stroke.getLineDashPattern());
            strokeLayer.setLineCapType(stroke.getCapType());
            strokeLayer.updateCircle(
                    circleShape.getPosition().getInitialPoint(),
                    circleShape.getSize().getInitialPoint());
            if (trim != null) {
                strokeLayer.setTrimPath(
                        trim.getStart().getInitialValue(),
                        trim.getEnd().getInitialValue());
            }

            addLayer(strokeLayer);
        }
    }

    private static final class LotteCircleShapeLayer extends LotteAnimatableLayer {
        private static final float ELLIPSE_CONTROL_POINT_PERCENTAGE = 0.55228f;

        private final RectF rect = new RectF();
        private final Paint paint = new Paint();
        private final Path path = new Path();
        private final Path trimPath = new Path();
        private final PathMeasure pathMeasure = new PathMeasure();

        private PointF circleSize;
        private PointF circlePosition;
        private List<Float> lineDashPattern;
        private LotteShapeStroke.LineCapType lineCapType;
        private LotteShapeStroke.LineJoinType lineJoinType;
        private float strokeStart;
        private float strokeEnd;

        public LotteCircleShapeLayer(long duration) {
            super(duration);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
        }

        public void updateCircle(PointF circlePosition, PointF circleSize) {
            this.circleSize = circleSize;
            this.circlePosition = circlePosition;
            float halfWidth = circleSize.x / 2f;
            float halfHeight = circleSize.y / 2f;

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

        public void setTrimPath(float strokeStart, float strokeEnd) {
            this.strokeStart = strokeStart;
            this.strokeEnd = strokeEnd;
            updateTrimPath();
            invalidateSelf();
        }

        private void updateTrimPath() {
            if (strokeStart != strokeEnd) {
                float length = pathMeasure.getLength();
                float start = length * strokeStart / 100f;
                float end = length * strokeEnd / 100f;

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

        public void setLineWidth(float width) {
            paint.setStrokeWidth(width);
        }

        public void setDashPattern(List<Float> lineDashPattern) {
            this.lineDashPattern = lineDashPattern;
        }

        public void setLineCapType(LotteShapeStroke.LineCapType lineCapType) {
            this.lineCapType = lineCapType;
        }

        public void setLineJoinType(LotteShapeStroke.LineJoinType lineJoinType) {
            this.lineJoinType = lineJoinType;
        }

        void setColor(@ColorInt int color) {
            paint.setColor(color);
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
