package com.airbnb.lottie.layers;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.airbnb.lottie.animation.LotteAnimatableValue;
import com.airbnb.lottie.animation.LotteAnimationGroup;
import com.airbnb.lottie.model.LotteShapeFill;
import com.airbnb.lottie.model.LotteShapePath;
import com.airbnb.lottie.model.LotteShapeStroke;
import com.airbnb.lottie.model.LotteShapeTransform;
import com.airbnb.lottie.model.LotteShapeTrimPath;
import com.airbnb.lottie.utils.LotteTransform3D;
import com.airbnb.lottie.utils.Observable;

import java.util.HashSet;
import java.util.Set;

class LotteShapeLayerView extends LotteAnimatableLayer {

    private final LotteShapePath path;
    private final LotteShapeFill fill;
    private final LotteShapeStroke stroke;
    private final LotteShapeTrimPath trim;
    private final LotteShapeTransform transformModel;

    @Nullable private LotteShapeLayer fillLayer;
    @Nullable private LotteShapeLayer strokeLayer;

    LotteShapeLayerView(LotteShapePath shape, @Nullable LotteShapeFill fill,
            @Nullable LotteShapeStroke stroke, @Nullable LotteShapeTrimPath trim,
            LotteShapeTransform transformModel, long duration, Drawable.Callback callback) {
        super(duration, callback);
        path = shape;
        this.fill = fill;
        this.stroke = stroke;
        this.trim = trim;
        this.transformModel = transformModel;

        setBounds(transformModel.getCompBounds());
        setAnchorPoint(transformModel.getAnchor().getObservable());
        setPosition(transformModel.getPosition().getObservable());
        setSublayerTransform(transformModel.getRotation().getObservable());

        Observable<LotteTransform3D> scale = transformModel.getScale().getObservable();
        setTransform(transformModel.getScale().getObservable());
        if (fill != null) {
            fillLayer = new LotteShapeLayer(getCallback());
            fillLayer.setPath(path.getShapePath().getObservable());
            fillLayer.setColor(fill.getColor().getObservable());
            fillLayer.setShapeAlpha(fill.getOpacity().getObservable());
            fillLayer.setTransformAlpha(transformModel.getOpacity().getObservable());
            fillLayer.setScale(scale);
            addLayer(fillLayer);
        }

        if (stroke != null) {
            strokeLayer = new LotteShapeLayer(getCallback());
            strokeLayer.setIsStroke();
            strokeLayer.setPath(path.getShapePath().getObservable());
            strokeLayer.setColor(stroke.getColor().getObservable());
            strokeLayer.setShapeAlpha(stroke.getOpacity().getObservable());
            strokeLayer.setTransformAlpha(transformModel.getOpacity().getObservable());
            strokeLayer.setLineWidth(stroke.getWidth().getObservable());
            strokeLayer.setDashPattern(stroke.getLineDashPattern(), stroke.getDashOffset());
            strokeLayer.setLineCapType(stroke.getCapType());
            strokeLayer.setLineJoinType(stroke.getJoinType());
            strokeLayer.setScale(scale);
            if (trim != null) {
                strokeLayer.setTrimPath(trim.getStart().getObservable(), trim.getEnd().getObservable());
            }
            addLayer(strokeLayer);
        }

        buildAnimation();
    }

    private void buildAnimation() {
        if (transformModel != null) {
            Set<LotteAnimatableValue> propertyAnimations = new HashSet<>();
            propertyAnimations.add(transformModel.getOpacity());
            propertyAnimations.add(transformModel.getPosition());
            propertyAnimations.add(transformModel.getAnchor());
            propertyAnimations.add(transformModel.getScale());
            propertyAnimations.add(transformModel.getRotation());
            addAnimation(new LotteAnimationGroup(propertyAnimations));
        }

        if (stroke != null && strokeLayer != null) {
            Set<LotteAnimatableValue> propertyAnimations = new HashSet<>();
            propertyAnimations.add(stroke.getColor());
            propertyAnimations.add(stroke.getOpacity());
            propertyAnimations.add(stroke.getWidth());
            propertyAnimations.add(path.getShapePath());
            if (!stroke.getLineDashPattern().isEmpty()) {
                propertyAnimations.add(stroke.getLineDashPattern().get(0));
                propertyAnimations.add(stroke.getLineDashPattern().get(1));
                propertyAnimations.add(stroke.getDashOffset());
            }
            if (trim != null) {
                propertyAnimations.add(trim.getStart());
                propertyAnimations.add(trim.getEnd());
                propertyAnimations.add(trim.getOffset());
            }
            strokeLayer.addAnimation(new LotteAnimationGroup(propertyAnimations));
        }

        if (fill != null && fillLayer != null) {
            Set<LotteAnimatableValue> propertyAnimations = new HashSet<>();
            propertyAnimations.add(fill.getColor());
            propertyAnimations.add(fill.getOpacity());
            propertyAnimations.add(path.getShapePath());
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
}
