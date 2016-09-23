package com.airbnb.lotte.layers;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.airbnb.lotte.animation.LotteAnimatableProperty;
import com.airbnb.lotte.animation.LotteAnimatableValue;
import com.airbnb.lotte.animation.LotteAnimationGroup;
import com.airbnb.lotte.model.LotteComposition;
import com.airbnb.lotte.model.LotteMask;
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
    private final Bitmap bitmap;
    private Bitmap maskBitmap;
    private Bitmap matteBitmap;
    private final Canvas contentCanvas;
    private Canvas maskCanvas;
    private Canvas matteCanvas;
    private final Paint maskShapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mattePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final LotteLayer layerModel;
    private final LotteComposition composition;

    private long parentId = -1;
    private LotteAnimationGroup animation;
    private LotteKeyframeAnimation inOutAnimation;
    private LotteAnimatableLayer childContainerLayer;


    public LotteLayerView(LotteLayer layerModel, LotteComposition composition, Drawable.Callback callback) {
        super(composition.getDuration(), callback);
        this.layerModel = layerModel;
        this.composition = composition;
        setBounds(composition.getBounds());
        bitmap = Bitmap.createBitmap(composition.getBounds().width(), composition.getBounds().height(), Bitmap.Config.ARGB_8888);
        maskPaint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        contentCanvas = new Canvas(bitmap);
        setupForModel(callback);
    }

    private void setupForModel(Drawable.Callback callback) {
        setBounds(composition.getBounds());
        anchorPoint = new Observable<>();
        anchorPoint.setValue(new PointF());

        childContainerLayer = new LotteAnimatableLayer(0, getCallback());
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
        childContainerLayer.anchorPoint = layerModel.getAnchor().getObservable();
        childContainerLayer.setTransform(layerModel.getScale().getObservable());
        childContainerLayer.sublayerTransform = new Observable<>(new LotteTransform3D());
        childContainerLayer.sublayerTransform.getValue().rotateZ(layerModel.getRotation().getInitialValue());
        mainCanvasPaint.setAlpha((int) layerModel.getOpacity().getInitialValue());

        setVisible(layerModel.isHasInAnimation(), false);

        List<Object> reversedItems = layerModel.getShapes();
        Collections.reverse(reversedItems);
        LotteShapeTransform currentTransform = null;
        LotteShapeTrimPath currentTrimPath = null;
        LotteShapeFill currentFill = null;
        LotteShapeStroke currentStroke = null;

        for (Object item : reversedItems) {
            if (item instanceof LotteShapeGroup) {
                LotteGroupLayerView groupLayer = new LotteGroupLayerView((LotteShapeGroup) item, currentFill,
                        currentStroke, currentTrimPath, currentTransform, duration, getCallback());
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


        if (layerModel.getMasks() != null) {
            mask = new LotteMaskLayer(layerModel.getMasks(), composition, getCallback());
            maskBitmap = Bitmap.createBitmap(
                    composition.getBounds().width(),
                    composition.getBounds().height(),
                    Bitmap.Config.ALPHA_8);
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
        childContainerLayer.addAnimation(new LotteAnimationGroup(propertyAnimations));

        if (layerModel.isHasInOutAnimation()) {
            inOutAnimation = new LotteNumberKeyframeAnimation<>(LotteAnimatableProperty.HIDDEN, layerModel.getCompDuration(),
                    layerModel.getInOutKeyTimes(), Long.class, layerModel.getInOutKeyFrames());
            inOutAnimation.setIsDiscrete();
            List<LotteKeyframeAnimation> animations = new ArrayList<>(1);
            animations.add(inOutAnimation);
            addAnimation(new LotteAnimationGroup(animations));
        }
    }

    public void setMask(LotteMaskLayer mask) {
        this.mask = mask;
    }

    public void setMatte(LotteLayerView matte) {
        this.matte = matte;
        matteBitmap = Bitmap.createBitmap(composition.getBounds().width(), composition.getBounds().height(), Bitmap.Config.ARGB_8888);
        matteCanvas = new Canvas(matteBitmap);
    }

    @Override
    public void draw(@NonNull Canvas mainCanvas) {
        bitmap.eraseColor(Color.TRANSPARENT);
        if (maskBitmap != null) {
            maskBitmap.eraseColor(Color.TRANSPARENT);
        }
        if (matteBitmap != null) {
            matteBitmap.eraseColor(Color.TRANSPARENT);
        }
        super.draw(contentCanvas);

        Bitmap mainBitmap;
        if (mask != null && !mask.getMasks().isEmpty()) {
            int maskSaveCount = maskCanvas.save();
            long parentId = this.parentId;
            while (parentId >= 0) {
                LotteLayer parent = composition.layerModelForId(parentId);
                applyTransformForLayer(maskCanvas, parent);
                parentId = parent.getParentId();
            }

            applyTransformForLayer(maskCanvas, layerModel);

            for (LotteMask m : mask.getMasks()) {
                maskCanvas.drawPath(m.getMaskPath().getInitialShape(), maskShapePaint);
            }
            maskCanvas.restoreToCount(maskSaveCount);
            if (matte == null) {
                mainCanvas.drawBitmap(maskBitmap, 0, 0, maskPaint);
            }
            mainBitmap = maskBitmap;
        } else {
            if (matte == null) {
                mainCanvas.drawBitmap(bitmap, 0, 0, mainCanvasPaint);
            }
            mainBitmap = bitmap;
        }

        if (matte != null) {
            matte.draw(matteCanvas);
            mattePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            matteCanvas.drawBitmap(mainBitmap, 0, 0, mattePaint);
            mainCanvas.drawBitmap(matteBitmap, 0, 0, mainCanvasPaint);
        }
    }

    private void applyTransformForLayer(Canvas canvas, LotteLayer layer) {
        if (layer.getPosition() != null) {
            canvas.translate(layer.getPosition().getInitialPoint().x, layer.getPosition().getInitialPoint().y);
        }
        if (layer.getScale().getInitialScale() != null) {
            canvas.scale(layer.getScale().getInitialScale().getScaleX(), layer.getScale().getInitialScale().getScaleY());
        }

        if (layer.getRotation().getInitialValue() != 0) {
            canvas.rotate(layer.getRotation().getInitialValue());
        }

        if (layer.getAnchor() != null) {
            canvas.translate(-layer.getAnchor().getInitialPoint().x, -layer.getAnchor().getInitialPoint().y);
        }
    }

    public long getId() {
        return layerModel.getId();
    }
}
