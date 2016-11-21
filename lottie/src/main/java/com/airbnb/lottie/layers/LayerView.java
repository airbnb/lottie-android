package com.airbnb.lottie.layers;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.animation.Interpolator;

import com.airbnb.lottie.animatable.AnimationGroup;
import com.airbnb.lottie.model.LottieComposition;
import com.airbnb.lottie.model.Layer;
import com.airbnb.lottie.model.ShapeFill;
import com.airbnb.lottie.model.ShapeGroup;
import com.airbnb.lottie.model.ShapeStroke;
import com.airbnb.lottie.model.ShapeTransform;
import com.airbnb.lottie.model.ShapeTrimPath;
import com.airbnb.lottie.animation.KeyframeAnimation;
import com.airbnb.lottie.animation.NumberKeyframeAnimation;
import com.airbnb.lottie.utils.ScaleXY;
import com.airbnb.lottie.animatable.Observable;

import java.util.Collections;
import java.util.List;

public class LayerView extends AnimatableLayer {

    /** CALayer#mask */
    private MaskLayer mask;
    private LayerView matte;

    private final Paint mainCanvasPaint = new Paint();
    @Nullable private final Bitmap contentBitmap;
    @Nullable private final Bitmap maskBitmap;
    @Nullable private final Bitmap matteBitmap;
    @Nullable private Canvas contentCanvas;
    @Nullable private Canvas maskCanvas;
    @Nullable private Canvas matteCanvas;
    private final Paint maskShapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mattePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Layer layerModel;
    private final LottieComposition composition;

    private long parentId = -1;
    private AnimatableLayer childContainerLayer;


    public LayerView(Layer layerModel, LottieComposition composition, Callback callback, @Nullable Bitmap mainBitmap, @Nullable Bitmap maskBitmap, @Nullable Bitmap matteBitmap) {
        super(composition.getDuration(), callback);
        this.layerModel = layerModel;
        this.composition = composition;
        this.maskBitmap = maskBitmap;
        this.matteBitmap = matteBitmap;
        this.contentBitmap = mainBitmap;
        mattePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        setBounds(composition.getBounds());
        if (contentBitmap != null) {
            contentCanvas = new Canvas(contentBitmap);
            if (maskBitmap != null) {
                maskPaint.setShader(new BitmapShader(contentBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            }
        }

        setupForModel(callback);
    }

    private void setupForModel(Drawable.Callback callback) {
        Observable<PointF> anchorPoint = new Observable<>();
        anchorPoint.setValue(new PointF());
        setAnchorPoint(anchorPoint);

        childContainerLayer = new AnimatableLayer(composition.getDuration(), getCallback());
        childContainerLayer.setCallback(callback);
        childContainerLayer.setBackgroundColor(layerModel.getSolidColor());
        childContainerLayer.setBounds(0, 0, layerModel.getSolidWidth(), layerModel.getSolidHeight());

        long parentId = layerModel.getParentId();
        AnimatableLayer currentChild = childContainerLayer;
        while (parentId >= 0) {
            if (parentId >= 0) {
                this.parentId = parentId;
            }
            Layer parentModel = composition.layerModelForId(parentId);
            ParentLayer parentLayer = new ParentLayer(parentModel, composition, getCallback());
            parentLayer.setCallback(callback);
            parentLayer.addLayer(currentChild);
            currentChild = parentLayer;
            parentId = parentModel.getParentId();
        }
        addLayer(currentChild);

        childContainerLayer.setPosition(layerModel.getPosition().getObservable());
        childContainerLayer.setAnchorPoint(layerModel.getAnchor().getObservable());
        childContainerLayer.setTransform(layerModel.getScale().getObservable());
        childContainerLayer.setRotation(layerModel.getRotation().getObservable());
        setAlpha(layerModel.getOpacity().getObservable());
        layerModel.getOpacity().getObservable().addChangeListener(new Observable.OnChangedListener() {
            @Override
            public void onChanged() {
                mainCanvasPaint.setAlpha(Math.round(layerModel.getOpacity().getObservable().getValue()));
                invalidateSelf();
            }
        });
        mainCanvasPaint.setAlpha(Math.round(layerModel.getOpacity().getObservable().getValue()));

        setVisible(layerModel.hasInAnimation(), false);

        List<Object> reversedItems = layerModel.getShapes();
        Collections.reverse(reversedItems);
        ShapeTransform currentTransform = null;
        ShapeTrimPath currentTrimPath = null;
        ShapeFill currentFill = null;
        ShapeStroke currentStroke = null;

        for (int i = 0; i < reversedItems.size(); i++) {
            Object item = reversedItems.get(i);
            if (item instanceof ShapeGroup) {
                GroupLayerView groupLayer = new GroupLayerView((ShapeGroup) item, currentFill,
                        currentStroke, currentTrimPath, currentTransform, compDuration, getCallback());
                childContainerLayer.addLayer(groupLayer);
            } else if (item instanceof ShapeTransform) {
                currentTransform = (ShapeTransform) item;
            } else if (item instanceof ShapeFill) {
                currentFill = (ShapeFill) item;
            } else if (item instanceof ShapeTrimPath) {
                currentTrimPath = (ShapeTrimPath) item;
            } else if (item instanceof ShapeStroke) {
                currentStroke = (ShapeStroke) item;
            }
        }

        if (maskBitmap != null && layerModel.getMasks() != null && !layerModel.getMasks().isEmpty()) {
            mask = new MaskLayer(layerModel.getMasks(), composition, getCallback());
            maskCanvas = new Canvas(maskBitmap);
        }
        buildAnimations();
    }

    private void buildAnimations() {
        childContainerLayer.addAnimation(layerModel.createAnimation());

        if (layerModel.hasInOutAnimation()) {
            NumberKeyframeAnimation<Float> inOutAnimation = new NumberKeyframeAnimation<>(
                    layerModel.getCompDuration(),
                    layerModel.getCompDuration(),
                    layerModel.getInOutKeyTimes(),
                    Float.class,
                    layerModel.getInOutKeyFrames(),
                    Collections.<Interpolator>emptyList());
            inOutAnimation.setIsDiscrete();
            inOutAnimation.addUpdateListener(new KeyframeAnimation.AnimationListener<Float>() {
                @Override
                public void onValueChanged(Float progress) {
                    setVisible(progress == 1f, false);
                }
            });
            setVisible(inOutAnimation.getValue() == 1f, false);
            addAnimation(AnimationGroup.forKeyframeAnimations(inOutAnimation));
        } else {
            setVisible(true, false);
        }
    }

    public void setMatte(LayerView matte) {
        if (matteBitmap == null) {
            throw new IllegalArgumentException("Cannot set a matte if no matte contentBitmap was given!");
        }
        this.matte = matte;
        matteCanvas = new Canvas(matteBitmap);
    }

    @Override
    public void draw(@NonNull Canvas mainCanvas) {
        if (contentBitmap != null) {
            if (contentBitmap.isRecycled()) {
                return;
            }
            contentBitmap.eraseColor(Color.TRANSPARENT);
        }
        if (maskBitmap != null) {
            maskBitmap.eraseColor(Color.TRANSPARENT);
        }
        if (matteBitmap != null) {
            matteBitmap.eraseColor(Color.TRANSPARENT);
        }
        if (!isVisible() || mainCanvasPaint.getAlpha() == 0) {
            return;
        }
        if (contentCanvas == null || contentBitmap == null) {
            super.draw(mainCanvas);
            return;
        }
        super.draw(contentCanvas);

        Bitmap mainBitmap;
        if (maskBitmap != null && maskCanvas != null && mask != null && !mask.getMasks().isEmpty()) {
            int maskSaveCount = maskCanvas.save();
            long parentId = this.parentId;
            while (parentId >= 0) {
                Layer parent = composition.layerModelForId(parentId);
                applyTransformForLayer(maskCanvas, parent);
                parentId = parent.getParentId();
            }

            applyTransformForLayer(maskCanvas, layerModel);

            for (int i = 0; i < mask.getMasks().size(); i++) {
                Path path = mask.getMasks().get(i).getMaskPath().getObservable().getValue();
                maskCanvas.drawPath(path, maskShapePaint);
            }
            maskCanvas.restoreToCount(maskSaveCount);
            if (matte == null) {
                mainCanvas.drawBitmap(maskBitmap, 0, 0, maskPaint);
            }
            mainBitmap = maskBitmap;
        } else {
            if (matte == null) {
                //noinspection ConstantConditions
                mainCanvas.drawBitmap(contentBitmap, 0, 0, mainCanvasPaint);
            }
            mainBitmap = contentBitmap;
        }

        if (matteCanvas != null && matteBitmap != null && matte != null) {
            matte.draw(matteCanvas);
            //noinspection ConstantConditions
            matteCanvas.drawBitmap(mainBitmap, 0, 0, mattePaint);
            mainCanvas.drawBitmap(matteBitmap, 0, 0, mainCanvasPaint);
        }
    }

    private void applyTransformForLayer(Canvas canvas, Layer layer) {
        PointF position = layer.getPosition().getObservable().getValue();
        if (position.x != 0 || position.y != 0) {
            canvas.translate(position.x, position.y);
        }

        ScaleXY scale = layer.getScale().getObservable().getValue();
        if (scale.getScaleX() != 1f || scale.getScaleY() != 1f) {
            canvas.scale(scale.getScaleX(), scale.getScaleY());
        }

        float rotation = layer.getRotation().getObservable().getValue();
        if (rotation != 0f) {
            canvas.rotate(rotation);
        }

        PointF translation = layer.getAnchor().getObservable().getValue();
        if (translation.x != 0 || translation.y != 0) {
            canvas.translate(-translation.x, -translation.y);
        }
    }

    @Override
    public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        super.setProgress(progress);
        if (matte != null) {
            matte.setProgress(progress);
        }
    }

    public long getId() {
        return layerModel.getId();
    }
}
