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
import com.airbnb.lotte.utils.Observable;

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
        anchorPoint = transformModel.getAnchor().getObservable();
        setPosition(transformModel.getPosition().getObservable());
        sublayerTransform = new Observable<>(new LotteTransform3D());
        sublayerTransform.getValue().rotateZ(transformModel.getRotation().getInitialValue());

        Observable<LotteTransform3D> initialScale = transformModel.getScale().getObservable();
        setTransform(transformModel.getScale().getObservable());
        if (fill != null) {
            fillLayer = new LotteShapeLayer(getCallback());
            fillLayer.setPath(path.getShapePath().getObservable());
            fillLayer.setColor(fill.getColor().getInitialColor());
            fillLayer.setShapeAlpha(fill.getOpacity().getObservable());
            fillLayer.setTransformAlpha(transformModel.getOpacity().getObservable());
            fillLayer.setScale(initialScale);
            addLayer(fillLayer);
        }

        if (stroke != null) {
            strokeLayer = new LotteShapeLayer(getCallback());
            strokeLayer.setStyle(Paint.Style.STROKE);
            strokeLayer.setPath(path.getShapePath().getObservable());
            strokeLayer.setColor(stroke.getColor().getInitialColor());
            strokeLayer.setShapeAlpha(stroke.getOpacity().getObservable());
            strokeLayer.setTransformAlpha(transformModel.getOpacity().getObservable());
            strokeLayer.setLineWidth(stroke.getWidth().getInitialValue());
            strokeLayer.setDashPattern(stroke.getLineDashPattern(), stroke.getDashOffset());
            strokeLayer.setLineCapType(stroke.getCapType());
            strokeLayer.setLineJoinType(stroke.getJoinType());
            strokeLayer.setScale(initialScale);
            if (trim != null) {
                strokeLayer.setTrimPath(trim.getStart().getObservable(), trim.getEnd().getObservable());
            }
            addLayer(strokeLayer);
        }

        buildAnimation();
    }

    private void buildAnimation() {
        SparseArray<LotteAnimatableValue> propertyAnimations = new SparseArray<>();
        if (transformModel != null) {
            propertyAnimations.put(LotteAnimatableProperty.OPACITY, transformModel.getOpacity());
            propertyAnimations.put(LotteAnimatableProperty.POSITION, transformModel.getPosition());
            propertyAnimations.put(LotteAnimatableProperty.ANCHOR_POINT, transformModel.getAnchor());
            propertyAnimations.put(LotteAnimatableProperty.TRANSFORM, transformModel.getScale());
            propertyAnimations.put(LotteAnimatableProperty.SUBLAYER_TRANSFORM, transformModel.getRotation());
        }

        if (stroke != null) {
            propertyAnimations.put(LotteAnimatableProperty.STROKE_COLOR, stroke.getColor());
            propertyAnimations.put(LotteAnimatableProperty.OPACITY, stroke.getOpacity());
            propertyAnimations.put(LotteAnimatableProperty.LINE_WIDTH, stroke.getWidth());
            propertyAnimations.put(LotteAnimatableProperty.PATH, path.getShapePath());
        }

        if (trim != null) {
            propertyAnimations.put(LotteAnimatableProperty.TRIM_PATH_START, trim.getStart());
            propertyAnimations.put(LotteAnimatableProperty.TRIM_PATH_END, trim.getEnd());
            propertyAnimations.put(LotteAnimatableProperty.TRIM_PATH_OFFSET, trim.getOffset());
        }
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
