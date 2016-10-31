package com.airbnb.lottie.layers;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.airbnb.lottie.model.ShapeFill;
import com.airbnb.lottie.model.ShapePath;
import com.airbnb.lottie.model.ShapeStroke;
import com.airbnb.lottie.model.ShapeTransform;
import com.airbnb.lottie.model.ShapeTrimPath;
import com.airbnb.lottie.utils.LottieTransform3D;
import com.airbnb.lottie.utils.Observable;

class ShapeLayerView extends AnimatableLayer {

    private final ShapePath path;
    private final ShapeFill fill;
    private final ShapeStroke stroke;
    private final ShapeTrimPath trim;
    private final ShapeTransform transformModel;

    @Nullable private ShapeLayer fillLayer;
    @Nullable private ShapeLayer strokeLayer;

    ShapeLayerView(ShapePath shape, @Nullable ShapeFill fill,
            @Nullable ShapeStroke stroke, @Nullable ShapeTrimPath trim,
            ShapeTransform transformModel, long duration, Drawable.Callback callback) {
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

        Observable<LottieTransform3D> scale = transformModel.getScale().getObservable();
        setTransform(transformModel.getScale().getObservable());
        if (fill != null) {
            fillLayer = new ShapeLayer(getCallback());
            fillLayer.setPath(path.getShapePath().getObservable());
            fillLayer.setColor(fill.getColor().getObservable());
            fillLayer.setShapeAlpha(fill.getOpacity().getObservable());
            fillLayer.setTransformAlpha(transformModel.getOpacity().getObservable());
            fillLayer.setScale(scale);
            addLayer(fillLayer);
        }

        if (stroke != null) {
            strokeLayer = new ShapeLayer(getCallback());
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
            addAnimation(transformModel.createAnimation());
        }

        if (stroke != null && strokeLayer != null) {
            strokeLayer.addAnimation(stroke.createAnimation());
            strokeLayer.addAnimation(path.createAnimation());
            if (trim != null) {
                strokeLayer.addAnimation(trim.createAnimation());
            }
        }

        if (fill != null && fillLayer != null) {
            fillLayer.addAnimation(fill.createAnimation());
            fillLayer.addAnimation(path.createAnimation());
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
