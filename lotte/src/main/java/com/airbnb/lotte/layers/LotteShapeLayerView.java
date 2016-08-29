package com.airbnb.lotte.layers;

import android.graphics.Paint;
import android.support.annotation.Nullable;

import com.airbnb.lotte.model.LotteShapeFill;
import com.airbnb.lotte.model.LotteShapePath;
import com.airbnb.lotte.model.LotteShapeStroke;
import com.airbnb.lotte.model.LotteShapeTransform;
import com.airbnb.lotte.model.LotteShapeTrimPath;
import com.airbnb.lotte.utils.LotteAnimationGroup;
import com.airbnb.lotte.utils.LotteTransform3D;

public class LotteShapeLayerView extends LotteAnimatableLayer {

    private final Paint fillPaint = new Paint();
    private final Paint strokePaint = new Paint();

    private final LotteShapePath path;
    private final LotteShapeFill fill;
    private final LotteShapeStroke stroke;
    private final LotteShapeTrimPath trim;
    private final LotteShapeTransform transformModel;

    private LotteShapeLayer fillLayer;
    private LotteShapeLayer strokeLayer;

    private LotteAnimationGroup animation;
    private LotteAnimationGroup strokeAnimation;
    private LotteAnimationGroup fillAnimation;

    public LotteShapeLayerView(LotteShapePath shape, @Nullable LotteShapeFill fill,
            @Nullable LotteShapeStroke stroke, @Nullable LotteShapeTrimPath trim,
            LotteShapeTransform transformModel, long duration) {
        super(duration);
        path = shape;
        this.fill = fill;
        this.stroke = stroke;
        this.trim = trim;
        this.transformModel = transformModel;

        fillPaint.setAlpha(0);
        fillPaint.setAntiAlias(true);
        strokePaint.setAntiAlias(true);

        setBounds(transformModel.getCompBounds());
        anchorPoint = transformModel.getAnchor().getInitialPoint();
        setAlpha((int) (transformModel.getOpacity().getInitialValue() * 255));
        position = transformModel.getPosition().getInitialPoint();
        sublayerTransform = new LotteTransform3D();
        sublayerTransform.rotateZ((float) Math.toDegrees(transformModel.getRotation().getInitialValue()));

        LotteTransform3D initialScale = transformModel.getScale().getInitialScale();
        this.transform = initialScale;
        if (fill != null) {
            fillLayer = new LotteShapeLayer();
            fillLayer.setPath(path.getShapePath().getInitialShape());
            fillLayer.setColor(fill.getColor().getInitialColor());
            fillLayer.setAlpha((int) (fill.getOpacity().getInitialValue() * 255));
            fillLayer.setScale(initialScale.getScaleX(), initialScale.getScaleY());
            addLayer(fillLayer);
        }

        if (stroke != null) {
            strokeLayer = new LotteShapeLayer();
            strokeLayer.setStyle(Paint.Style.STROKE);
            strokeLayer.setPath(path.getShapePath().getInitialShape());
            strokeLayer.setColor(stroke.getColor().getInitialColor());
            strokeLayer.setAlpha((int) (stroke.getOpacity().getInitialValue() * 255));
            strokeLayer.setLineWidth(stroke.getWidth().getInitialValue());
            strokeLayer.setDashPattern(stroke.getLineDashPattern());
            strokeLayer.setLineCapType(stroke.getCapType());
            strokeLayer.setLineJoinType(stroke.getJoinType());
            strokeLayer.setScale(initialScale.getScaleX(), initialScale.getScaleY());

            if (trim != null) {
                strokeLayer.setStrokeStart(trim.getStart().getInitialValue());
                strokeLayer.setStrokeEnd(trim.getEnd().getInitialValue());
            }
            addLayer(strokeLayer);
        }

        buildAnimation();
    }

    private void buildAnimation() {
        // TODO
    }

    public LotteShapeFill getFill() {
        return fill;
    }

    public LotteShapePath getShape() {
        return path;
    }

    public LotteShapeStroke getStroke() {
        return stroke;
    }

    public LotteShapeTransform getTransform() {
        return transformModel;
    }

    public LotteShapeTrimPath getTrim() {
        return trim;
    }
}
