package com.airbnb.lottie.layers;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.airbnb.lottie.model.LottieShapeFill;
import com.airbnb.lottie.model.LottieShapePath;
import com.airbnb.lottie.model.LottieShapeStroke;
import com.airbnb.lottie.model.LottieShapeTransform;
import com.airbnb.lottie.model.LottieShapeTrimPath;
import com.airbnb.lottie.utils.LottieTransform3D;
import com.airbnb.lottie.utils.Observable;

class LottieShapeLayerView extends LottieAnimatableLayer {

    private final LottieShapePath path;
    private final LottieShapeFill fill;
    private final LottieShapeStroke stroke;
    private final LottieShapeTrimPath trim;
    private final LottieShapeTransform transformModel;

    @Nullable private LottieShapeLayer fillLayer;
    @Nullable private LottieShapeLayer strokeLayer;

    LottieShapeLayerView(LottieShapePath shape, @Nullable LottieShapeFill fill,
            @Nullable LottieShapeStroke stroke, @Nullable LottieShapeTrimPath trim,
            LottieShapeTransform transformModel, long duration, Drawable.Callback callback) {
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
            fillLayer = new LottieShapeLayer(getCallback());
            fillLayer.setPath(path.getShapePath().getObservable());
            fillLayer.setColor(fill.getColor().getObservable());
            fillLayer.setShapeAlpha(fill.getOpacity().getObservable());
            fillLayer.setTransformAlpha(transformModel.getOpacity().getObservable());
            fillLayer.setScale(scale);
            addLayer(fillLayer);
        }

        if (stroke != null) {
            strokeLayer = new LottieShapeLayer(getCallback());
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
