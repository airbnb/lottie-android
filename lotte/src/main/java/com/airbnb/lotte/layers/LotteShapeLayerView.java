package com.airbnb.lotte.layers;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.airbnb.lotte.animation.LotteAnimatableProperty;
import com.airbnb.lotte.animation.LotteAnimatableValue;
import com.airbnb.lotte.model.LotteShapeFill;
import com.airbnb.lotte.model.LotteShapePath;
import com.airbnb.lotte.model.LotteShapeStroke;
import com.airbnb.lotte.model.LotteShapeTransform;
import com.airbnb.lotte.model.LotteShapeTrimPath;
import com.airbnb.lotte.animation.LotteAnimationGroup;
import com.airbnb.lotte.utils.LotteTransform3D;

public class LotteShapeLayerView extends LotteAnimatableLayer {

    private final Paint fillPaint = new Paint();
    private final Paint strokePaint = new Paint();

    private final LotteShapePath path;
    private final LotteShapeFill fill;
    private final LotteShapeStroke stroke;
    private final LotteShapeTrimPath trim;
    private final LotteShapeTransform transformModel;

    @Nullable private LotteShapeLayer fillLayer;
    @Nullable private LotteShapeLayer strokeLayer;

    private LotteAnimationGroup animation;
    private LotteAnimationGroup strokeAnimation;
    private LotteAnimationGroup fillAnimation;

    public LotteShapeLayerView(LotteShapePath shape, @Nullable LotteShapeFill fill,
            @Nullable LotteShapeStroke stroke, @Nullable LotteShapeTrimPath trim,
            LotteShapeTransform transformModel, long duration, Drawable.Callback callback) {
        super(duration, callback);
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
        position = transformModel.getPosition().getObservable();
        sublayerTransform = new LotteTransform3D();
        sublayerTransform.rotateZ(transformModel.getRotation().getInitialValue());

        LotteTransform3D initialScale = transformModel.getScale().getInitialScale();
        this.transform = initialScale;
        if (fill != null) {
            fillLayer = new LotteShapeLayer();
            fillLayer.setPath(path.getShapePath().getInitialShape());
            fillLayer.setColor(fill.getColor().getInitialColor());
            fillLayer.setShapeAlpha((int) (fill.getOpacity().getInitialValue()));
            fillLayer.setTransformAlpha((int) transformModel.getOpacity().getInitialValue());
            fillLayer.setScale(initialScale.getScaleX(), initialScale.getScaleY());
            addLayer(fillLayer);
        }

        if (stroke != null) {
            strokeLayer = new LotteShapeLayer();
            strokeLayer.setStyle(Paint.Style.STROKE);
            strokeLayer.setPath(path.getShapePath().getInitialShape());
            strokeLayer.setColor(stroke.getColor().getInitialColor());
            strokeLayer.setShapeAlpha((int) (stroke.getOpacity().getInitialValue()));
            strokeLayer.setTransformAlpha((int) transformModel.getOpacity().getInitialValue());
            strokeLayer.setLineWidth(stroke.getWidth().getInitialValue());
            strokeLayer.setDashPattern(stroke.getLineDashPattern(), stroke.getDashOffset());
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
        SparseArray<LotteAnimatableValue> propertyAnimations = new SparseArray<>();
        propertyAnimations.put(LotteAnimatableProperty.OPACITY, transformModel.getOpacity());
        propertyAnimations.put(LotteAnimatableProperty.POSITION, transformModel.getPosition());
        propertyAnimations.put(LotteAnimatableProperty.ANCHOR_POINT, transformModel.getAnchor());
        propertyAnimations.put(LotteAnimatableProperty.TRANSFORM, transformModel.getScale());
        propertyAnimations.put(LotteAnimatableProperty.SUBLAYER_TRANSFORM, transformModel.getRotation());
        addAnimation(new LotteAnimationGroup(propertyAnimations));
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
