package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.graphics.Paint;
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
        // TODO
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
            fillLayer.circlePosition = circleShape.getPosition().getInitialPoint();
            fillLayer.circleSize = circleShape.getSize().getInitialPoint();
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
            strokeLayer.circlePosition = circleShape.getPosition().getInitialPoint();
            strokeLayer.circleSize = circleShape.getSize().getInitialPoint();
            addLayer(strokeLayer);
        }
    }

    private static final class LotteCircleShapeLayer extends LotteAnimatableLayer {

        private final RectF rect = new RectF();
        private final Paint paint = new Paint();

        private PointF circleSize;
        private PointF circlePosition;
        private List<Float> lineDashPattern;
        private LotteShapeStroke.LineCapType lineCapType;
        private LotteShapeStroke.LineJoinType lineJoinType;

        public LotteCircleShapeLayer(long duration) {
            super(duration);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
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
            float halfWidth = circleSize.x / 2f;
            float halfHeight = circleSize.y / 2f;
            rect.set(circlePosition.x - halfWidth,
                    circlePosition.y - halfHeight,
                    circlePosition.x + halfHeight,
                    circlePosition.y + halfHeight);
            canvas.drawOval(rect, paint);
        }
    }
}
