package com.airbnb.lotte.layers;

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
import android.util.SparseArray;

import com.airbnb.lotte.animation.LotteAnimatableProperty;
import com.airbnb.lotte.animation.LotteAnimatableValue;
import com.airbnb.lotte.animation.LotteAnimationGroup;
import com.airbnb.lotte.model.LotteComposition;
import com.airbnb.lotte.model.LotteShapeFill;
import com.airbnb.lotte.model.LotteShapeGroup;
import com.airbnb.lotte.model.LotteShapeStroke;
import com.airbnb.lotte.model.LotteShapeTransform;
import com.airbnb.lotte.model.LotteShapeTrimPath;
import com.airbnb.lotte.utils.LotteKeyframeAnimation;
import com.airbnb.lotte.utils.LotteNumberKeyframeAnimation;
import com.airbnb.lotte.utils.LotteTransform3D;
import com.airbnb.lotte.utils.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class LotteLayerView extends LotteAnimatableLayer {

    /** CALayer#mask */
    private LotteMaskLayer mask;
    private LotteLayerView matte;

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

    private final LotteLayer layerModel;
    private final LotteComposition composition;

    private long parentId = -1;
    private LotteAnimationGroup animation;
    private LotteAnimatableLayer childContainerLayer;
    private Observable<Number> opacity;


    public LotteLayerView(LotteLayer layerModel, LotteComposition composition, Callback callback, @Nullable Bitmap mainBitmap, @Nullable Bitmap maskBitmap, @Nullable Bitmap matteBitmap) {
        super(composition.getDuration(), callback);
        this.layerModel = layerModel;
        this.composition = composition;
        this.maskBitmap = maskBitmap;
        this.matteBitmap = matteBitmap;
        this.contentBitmap = mainBitmap;
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

        childContainerLayer = new LotteAnimatableLayer(composition.getDuration(), getCallback());
        childContainerLayer.setCallback(callback);
        childContainerLayer.setBackgroundColor(layerModel.getSolidColor());
        childContainerLayer.setBounds(0, 0, layerModel.getSolidWidth(), layerModel.getSolidHeight());

        long parentId = layerModel.getParentId();
        LotteAnimatableLayer currentChild = childContainerLayer;
        while (parentId >= 0) {
            if (parentId >= 0) {
                this.parentId = parentId;
            }
            LotteLayer parentModel = composition.layerModelForId(parentId);
            LotteParentLayer parentLayer = new LotteParentLayer(parentModel, composition, getCallback());
            parentLayer.setCallback(callback);
            parentLayer.addLayer(currentChild);
            currentChild = parentLayer;
            parentId = parentModel.getParentId();
        }
        addLayer(currentChild);

        childContainerLayer.setPosition(layerModel.getPosition().getObservable());
        childContainerLayer.setAnchorPoint(layerModel.getAnchor().getObservable());
        childContainerLayer.setTransform(layerModel.getScale().getObservable());
        childContainerLayer.setSublayerTransform(layerModel.getRotation().getObservable());
        layerModel.getOpacity().getObservable().addChangeListener(new Observable.OnChangedListener() {
            @Override
            public void onChanged() {
                mainCanvasPaint.setAlpha(Math.round((float) layerModel.getOpacity().getObservable().getValue()));
                invalidateSelf();
            }
        });
        mainCanvasPaint.setAlpha(Math.round((float) layerModel.getOpacity().getObservable().getValue()));

        setVisible(layerModel.hasInAnimation(), false);

        List<Object> reversedItems = layerModel.getShapes();
        Collections.reverse(reversedItems);
        LotteShapeTransform currentTransform = null;
        LotteShapeTrimPath currentTrimPath = null;
        LotteShapeFill currentFill = null;
        LotteShapeStroke currentStroke = null;

        for (int i = 0; i < reversedItems.size(); i++) {
            Object item = reversedItems.get(i);
            if (item instanceof LotteShapeGroup) {
                LotteGroupLayerView groupLayer = new LotteGroupLayerView((LotteShapeGroup) item, currentFill,
                        currentStroke, currentTrimPath, currentTransform, compDuration, getCallback());
                childContainerLayer.addLayer(groupLayer);
            } else if (item instanceof LotteShapeTransform) {
                currentTransform = (LotteShapeTransform) item;
            } else if (item instanceof LotteShapeFill) {
                currentFill = (LotteShapeFill) item;
            } else if (item instanceof LotteShapeTrimPath) {
                currentTrimPath = (LotteShapeTrimPath) item;
            } else if (item instanceof LotteShapeStroke) {
                currentStroke = (LotteShapeStroke) item;
            }
        }

        if (maskBitmap != null && layerModel.getMasks() != null && !layerModel.getMasks().isEmpty()) {
            mask = new LotteMaskLayer(layerModel.getMasks(), composition, getCallback());
            maskCanvas = new Canvas(maskBitmap);
        }
        buildAnimations();
    }

    private void buildAnimations() {
        SparseArray<LotteAnimatableValue> propertyAnimations = new SparseArray<>();
        propertyAnimations.put(LotteAnimatableProperty.OPACITY, layerModel.getOpacity());
        propertyAnimations.put(LotteAnimatableProperty.POSITION, layerModel.getPosition());
        propertyAnimations.put(LotteAnimatableProperty.ANCHOR_POINT, layerModel.getAnchor());
        propertyAnimations.put(LotteAnimatableProperty.TRANSFORM, layerModel.getScale());
        propertyAnimations.put(LotteAnimatableProperty.SUBLAYER_TRANSFORM, layerModel.getRotation());
        childContainerLayer.addAnimation(new LotteAnimationGroup(propertyAnimations, layerModel.getCompDuration()));

        if (layerModel.hasInOutAnimation()) {
            LotteNumberKeyframeAnimation<Float> inOutAnimation = new LotteNumberKeyframeAnimation<>(
                    LotteAnimatableProperty.HIDDEN,
                    layerModel.getCompDuration(),
                    layerModel.getCompDuration(),
                    layerModel.getInOutKeyTimes(),
                    Float.class,
                    layerModel.getInOutKeyFrames());
            inOutAnimation.setIsDiscrete();
            inOutAnimation.addUpdateListener(new LotteKeyframeAnimation.AnimationListener<Float>() {
                @Override
                public void onValueChanged(Float progress) {
                    setVisible(progress == 1f, false);
                }
            });
            setVisible(inOutAnimation.getValueForProgress(0f) == 1f, false);
            List<LotteKeyframeAnimation> animations = new ArrayList<>(1);
            animations.add(inOutAnimation);
            addAnimation(new LotteAnimationGroup(animations));
        } else {
            setVisible(true, false);
        }
    }

    public void setMask(LotteMaskLayer mask) {
        this.mask = mask;
    }

    public void setMatte(LotteLayerView matte) {
        if (matteBitmap == null) {
            throw new IllegalArgumentException("Cannot set a matte if no matte contentBitmap was given!");
        }
        this.matte = matte;
        matteCanvas = new Canvas(matteBitmap);
    }

    @Override
    public void draw(@NonNull Canvas mainCanvas) {
        if (contentBitmap != null) {
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
                LotteLayer parent = composition.layerModelForId(parentId);
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
            mattePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            //noinspection ConstantConditions
            matteCanvas.drawBitmap(mainBitmap, 0, 0, mattePaint);
            mainCanvas.drawBitmap(matteBitmap, 0, 0, mainCanvasPaint);
        }
    }

    private void applyTransformForLayer(Canvas canvas, LotteLayer layer) {
        PointF position = layer.getPosition().getObservable().getValue();
        if (position.x != 0 || position.y != 0) {
            canvas.translate(position.x, position.y);
        }

        LotteTransform3D scale = layer.getScale().getObservable().getValue();
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
